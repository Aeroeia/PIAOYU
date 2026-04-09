package com.damai.context.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.damai.ai.function.AiProgram;
import com.damai.ai.function.call.UserCall;
import com.damai.ai.function.dto.CreateOrderFunctionDto;
import com.damai.ai.function.dto.ProgramSearchFunctionDto;
import com.damai.context.model.AiOrderExecutionStatus;
import com.damai.context.model.OrderSlots;
import com.damai.entity.AiOrderExecutionState;
import com.damai.vo.CreateOrderVo;
import com.damai.vo.ProgramDetailVo;
import com.damai.vo.TicketCategoryVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 下单 Plan-Execute 服务：通过显式状态机执行下单，并把状态持久化用于多轮续跑。
 * 该服务只负责编排和校验，真实下单仍复用既有业务能力（AiProgram）。
 */
@Service
@Slf4j
public class OrderPlanExecuteService {


    private static final Pattern MOBILE_PATTERN = Pattern.compile("1[3-9]\\d{9}");
    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d{2,5})(?:\\s*)(?:元|块)");
    private static final Pattern COUNT_PATTERN = Pattern.compile("(?:买|要|订|下单)?\\s*(\\d{1,2})\\s*张");
    private static final Pattern DATE_PATTERN = Pattern.compile("(20\\d{2}[-/.]\\d{1,2}[-/.]\\d{1,2})");
    private static final Pattern ID_NUMBER_PATTERN = Pattern.compile("[0-9Xx*]{8,18}");
    private static final Pattern ACTOR_PATTERN = Pattern.compile("([\\u4e00-\\u9fa5A-Za-z0-9·]{2,24})(?:演唱会|音乐节|巡演|话剧)");
    private static final int MAX_RETRY = 3;

    private static final List<String> CITY_CANDIDATES = List.of(
            "北京", "上海", "广州", "深圳", "杭州", "成都", "重庆", "武汉", "南京", "西安",
            "苏州", "天津", "长沙", "厦门", "福州", "郑州", "青岛", "宁波", "无锡", "合肥",
            "南昌", "济南", "昆明", "沈阳", "大连", "珠海", "佛山"
    );

    private static final List<String> ORDER_CONFIRM_KEYS = List.of(
            "确认下单", "确认支付", "继续下单", "继续支付", "立即下单", "马上下单", "确定购买", "确认购买", "yes"
    );

    private final AiOrderExecutionStateService aiOrderExecutionStateService;
    private final AiProgram aiProgram;
    private final UserCall userCall;
    private final ChatClient tinyIntentChatClient;

    public OrderPlanExecuteService(AiOrderExecutionStateService aiOrderExecutionStateService,
                                   AiProgram aiProgram,
                                   UserCall userCall,
                                   @Qualifier("titleChatClient") ChatClient tinyIntentChatClient) {
        this.aiOrderExecutionStateService = aiOrderExecutionStateService;
        this.aiProgram = aiProgram;
        this.userCall = userCall;
        this.tinyIntentChatClient = tinyIntentChatClient;
    }

    public String handle(Integer chatType,
                         String chatId,
                         Long userId,
                         String prompt,
                         String systemContext) {
        AiOrderExecutionState executionState = aiOrderExecutionStateService.getOrCreate(chatType, chatId, userId);
        OrderSlots persistedSlots = parseSlots(executionState.getSlotsJson());
        OrderSlots incomingSlots = extractSlots(prompt, systemContext);
        OrderSlots mergedSlots = mergeSlots(persistedSlots, incomingSlots);

        executionState.setSlotsJson(JSON.toJSONString(mergedSlots));
        updateIdempotencyKey(executionState, chatType, chatId, mergedSlots);
        persistPlan(executionState, mergedSlots, null, null);
        aiOrderExecutionStateService.update(executionState);

        return resume(executionState, mergedSlots, prompt);
    }

    public String plan(AiOrderExecutionState executionState, OrderSlots slots) {
        List<String> missingSlots = collectMissingSlots(slots);
        if (!missingSlots.isEmpty()) {
            executionState.setState(AiOrderExecutionStatus.WAITING_USER_INPUT.name());
            executionState.setCurrentStep(AiOrderExecutionStatus.CHECK.name());
            persistPlan(executionState, slots, missingSlots, null);
            aiOrderExecutionStateService.update(executionState);
            return "为了继续下单，我还缺这些信息：" + String.join("、", missingSlots) + "。请补充后我会从中断步骤继续执行。";
        }

        executionState.setState(AiOrderExecutionStatus.CHECK.name());
        executionState.setCurrentStep(AiOrderExecutionStatus.CHECK.name());
        persistPlan(executionState, slots, List.of(), null);
        aiOrderExecutionStateService.update(executionState);
        return null;
    }

    /**
     * execute 负责单次状态推进，状态不足时返回等待用户补全/确认的文案。
     */
    public String execute(AiOrderExecutionState executionState, OrderSlots slots, String prompt) {
        AiOrderExecutionStatus currentState = parseState(executionState.getState());

        switch (currentState) {
            case INIT:
            case WAITING_USER_INPUT:
                String planResult = plan(executionState, slots);
                if (planResult != null) {
                    return planResult;
                }
                return execute(executionState, slots, prompt);
            case CHECK:
                return runCheck(executionState, slots, prompt);
            case LOCK:
                return runLock(executionState, slots, prompt);
            case PAY:
                return runPayGate(executionState, slots, prompt);
            case CONFIRM:
                return runConfirm(executionState, slots);
            case DONE:
                return buildDoneMessage(executionState);
            case FAILED:
                if (shouldRetry(prompt, executionState)) {
                    executionState.setState(AiOrderExecutionStatus.CHECK.name());
                    executionState.setCurrentStep(AiOrderExecutionStatus.CHECK.name());
                    aiOrderExecutionStateService.update(executionState);
                    return execute(executionState, slots, prompt);
                }
                return "当前下单流程处于失败状态。你可以补充或修正参数后继续，例如：城市、艺人、票档价位、张数、手机号、购票人证件号。";
            default:
                executionState.setState(AiOrderExecutionStatus.INIT.name());
                executionState.setCurrentStep(AiOrderExecutionStatus.INIT.name());
                aiOrderExecutionStateService.update(executionState);
                return execute(executionState, slots, prompt);
        }
    }

    /**
     * resume 负责在同一轮请求内串行推进状态，直到需要用户输入或流程完成。
     */
    public String resume(AiOrderExecutionState executionState, OrderSlots slots, String prompt) {
        int guard = 0;
        while (guard++ < 8) {
            String result = execute(executionState, slots, prompt);
            AiOrderExecutionStatus state = parseState(executionState.getState());
            if (state == AiOrderExecutionStatus.CHECK
                    || state == AiOrderExecutionStatus.LOCK
                    || state == AiOrderExecutionStatus.PAY
                    || state == AiOrderExecutionStatus.CONFIRM) {
                continue;
            }
            return result;
        }
        return "下单流程处理轮次已达上限，请稍后重试或重新描述你的下单需求。";
    }

    private String runCheck(AiOrderExecutionState executionState, OrderSlots slots, String prompt) {
        try {
            ProgramDetailVo detailVo = aiProgram.selectTicketCategory(toProgramSearch(slots));
            if (detailVo == null || detailVo.getId() == null) {
                return markFailed(executionState, slots, "未查询到匹配节目，请调整城市、艺人或演出时间。", true);
            }
            userCall.userDetail(slots.getMobile());

            executionState.setState(AiOrderExecutionStatus.LOCK.name());
            executionState.setCurrentStep(AiOrderExecutionStatus.LOCK.name());
            aiOrderExecutionStateService.update(executionState);
            return execute(executionState, slots, prompt);
        } catch (Exception e) {
            return markFailed(executionState, slots, "下单预校验失败：" + safeMessage(e), true);
        }
    }

    private String runLock(AiOrderExecutionState executionState, OrderSlots slots, String prompt) {
        try {
            ProgramDetailVo detailVo = aiProgram.selectTicketCategory(toProgramSearch(slots));
            if (detailVo == null || detailVo.getTicketCategoryVoList() == null || detailVo.getTicketCategoryVoList().isEmpty()) {
                return markFailed(executionState, slots, "票档信息不存在，请更换节目或票档。", true);
            }
            TicketCategoryVo matchedTicket = matchTicketByPrice(detailVo.getTicketCategoryVoList(), slots.getTicketCategoryPrice());
            if (matchedTicket == null) {
                return markFailed(executionState, slots, "未匹配到该票档价格，请确认票档价位。", true);
            }
            long remain = matchedTicket.getRemainNumber() == null ? 0L : matchedTicket.getRemainNumber();
            if (remain < (slots.getTicketCount() == null ? 0 : slots.getTicketCount())) {
                return markFailed(executionState, slots, "该票档余票不足，请减少张数或更换票档。", true);
            }

            executionState.setState(AiOrderExecutionStatus.PAY.name());
            executionState.setCurrentStep(AiOrderExecutionStatus.PAY.name());
            aiOrderExecutionStateService.update(executionState);
            return execute(executionState, slots, prompt);
        } catch (Exception e) {
            return markFailed(executionState, slots, "库存锁定校验失败：" + safeMessage(e), true);
        }
    }

    private String runPayGate(AiOrderExecutionState executionState, OrderSlots slots, String prompt) {
        // 支付确认门：未收到明确确认前停在 WAITING_USER_INPUT，避免误触发下单。
        if (!isConfirmPrompt(prompt)) {
            executionState.setState(AiOrderExecutionStatus.WAITING_USER_INPUT.name());
            executionState.setCurrentStep(AiOrderExecutionStatus.PAY.name());
            aiOrderExecutionStateService.update(executionState);
            return """
                    我已完成参数校验和库存检查，请确认是否继续下单：
                    - 城市：%s
                    - 艺人：%s
                    - 票档：%s 元
                    - 张数：%s
                    - 手机号：%s
                    回复“确认下单”后我会继续执行。
                    """.formatted(
                    safeText(slots.getCityName()),
                    safeText(slots.getActor()),
                    slots.getTicketCategoryPrice() == null ? "-" : slots.getTicketCategoryPrice().toPlainString(),
                    slots.getTicketCount() == null ? "-" : slots.getTicketCount(),
                    safeText(slots.getMobile())
            ).trim();
        }

        executionState.setState(AiOrderExecutionStatus.CONFIRM.name());
        executionState.setCurrentStep(AiOrderExecutionStatus.CONFIRM.name());
        aiOrderExecutionStateService.update(executionState);
        return execute(executionState, slots, prompt);
    }

    private String runConfirm(AiOrderExecutionState executionState, OrderSlots slots) {
        try {
            CreateOrderVo createOrderVo = aiProgram.createOrder(toCreateOrderDto(slots));
            executionState.setState(AiOrderExecutionStatus.DONE.name());
            executionState.setCurrentStep(AiOrderExecutionStatus.DONE.name());
            persistPlan(executionState, slots, List.of(), createOrderVo);
            aiOrderExecutionStateService.update(executionState);
            return """
                    下单成功，订单号：%s
                    你可以前往订单页继续支付：%s
                    """.formatted(createOrderVo.getOrderNumber(), createOrderVo.getOrderListAddress()).trim();
        } catch (Exception e) {
            return markFailed(executionState, slots, "订单创建失败：" + safeMessage(e), true);
        }
    }

    private String markFailed(AiOrderExecutionState executionState,
                              OrderSlots slots,
                              String error,
                              boolean recoverable) {
        executionState.setState(AiOrderExecutionStatus.FAILED.name());
        executionState.setCurrentStep(AiOrderExecutionStatus.FAILED.name());
        executionState.setRetryCount((executionState.getRetryCount() == null ? 0 : executionState.getRetryCount()) + 1);
        persistPlan(executionState, slots, null, null);
        aiOrderExecutionStateService.update(executionState);

        if (recoverable && (executionState.getRetryCount() == null || executionState.getRetryCount() <= MAX_RETRY)) {
            return error + " 你可以补充或修正参数后继续，我会从失败步骤续跑。";
        }
        return error + " 当前重试次数较多，建议稍后重试或重新发起新会话。";
    }

    private String buildDoneMessage(AiOrderExecutionState executionState) {
        JSONObject planObject = parseJsonObject(executionState.getPlanJson());
        String orderNumber = planObject.getString("orderNumber");
        String orderAddress = planObject.getString("orderListAddress");
        if (orderNumber != null && !orderNumber.isBlank()) {
            return """
                    该会话下单已完成，订单号：%s
                    订单地址：%s
                    如果你想重新下单，可以直接提供新的城市/艺人/票档信息。
                    """.formatted(orderNumber, safeText(orderAddress)).trim();
        }
        return "该会话下单已完成。如果你想重新下单，可以直接提供新的城市/艺人/票档信息。";
    }

    private void persistPlan(AiOrderExecutionState executionState,
                             OrderSlots slots,
                             List<String> missingSlots,
                             CreateOrderVo createOrderVo) {
        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("steps", List.of("INIT", "CHECK", "LOCK", "PAY", "CONFIRM", "DONE"));
        plan.put("currentStep", executionState.getCurrentStep());
        plan.put("state", executionState.getState());
        plan.put("idempotencyKey", executionState.getIdempotencyKey());
        if (missingSlots != null) {
            plan.put("missingSlots", missingSlots);
        }
        if (createOrderVo != null) {
            plan.put("orderNumber", createOrderVo.getOrderNumber());
            plan.put("orderListAddress", createOrderVo.getOrderListAddress());
        }
        executionState.setPlanJson(JSON.toJSONString(plan));
    }

    private void updateIdempotencyKey(AiOrderExecutionState executionState,
                                      Integer chatType,
                                      String chatId,
                                      OrderSlots slots) {
        if (!collectMissingSlots(slots).isEmpty()) {
            return;
        }
        String seed = chatType + "|" + chatId + "|" + safeText(slots.getCityName()) + "|" + safeText(slots.getActor())
                + "|" + safeText(slots.getShowTime()) + "|" + safeText(slots.getMobile()) + "|"
                + (slots.getTicketCategoryPrice() == null ? "" : slots.getTicketCategoryPrice().toPlainString())
                + "|" + (slots.getTicketCount() == null ? "" : slots.getTicketCount())
                + "|" + safeText(slots.getTicketUserNumberList() == null ? "" : String.join(",", slots.getTicketUserNumberList()));
        executionState.setIdempotencyKey(md5Base64(seed));
    }

    private OrderSlots extractSlots(String prompt, String systemContext) {
        OrderSlots slotsByModel = extractSlotsByModel(prompt, systemContext);
        OrderSlots slotsByRule = extractSlotsByRule(prompt);
        return mergeSlots(slotsByRule, slotsByModel);
    }

    private OrderSlots extractSlotsByModel(String prompt, String systemContext) {
        OrderSlots slots = new OrderSlots();
        try {
            String request = """
                    你是下单参数抽取器。请从用户输入中提取下列字段并返回JSON，不存在则返回null：
                    {
                      "cityName": "string|null",
                      "actor": "string|null",
                      "showTime": "yyyy-MM-dd|null",
                      "mobile": "string|null",
                      "ticketCategoryPrice": number|null,
                      "ticketCount": number|null,
                      "ticketUserNumberList": ["string",...]
                    }
                    只返回JSON，不要输出解释。
                    已有上下文：%s
                    用户输入：%s
                    """.formatted(safeText(systemContext), safeText(prompt));

            String content = tinyIntentChatClient.prompt()
                    .user(request)
                    .call()
                    .content();
            JSONObject jsonObject = JSON.parseObject(extractJson(content));

            slots.setCityName(blankToNull(jsonObject.getString("cityName")));
            slots.setActor(blankToNull(jsonObject.getString("actor")));
            slots.setShowTime(blankToNull(jsonObject.getString("showTime")));
            slots.setMobile(blankToNull(jsonObject.getString("mobile")));
            Object priceObj = jsonObject.get("ticketCategoryPrice");
            if (priceObj != null && !String.valueOf(priceObj).isBlank() && !"null".equalsIgnoreCase(String.valueOf(priceObj))) {
                slots.setTicketCategoryPrice(new BigDecimal(String.valueOf(priceObj)));
            }
            Integer ticketCount = jsonObject.getInteger("ticketCount");
            slots.setTicketCount(ticketCount);
            JSONArray idArray = jsonObject.getJSONArray("ticketUserNumberList");
            if (idArray != null && !idArray.isEmpty()) {
                List<String> ids = new ArrayList<>();
                for (int i = 0; i < idArray.size(); i++) {
                    String id = idArray.getString(i);
                    if (id != null && !id.isBlank()) {
                        ids.add(id.trim());
                    }
                }
                if (!ids.isEmpty()) {
                    slots.setTicketUserNumberList(ids);
                }
            }
        } catch (Exception e) {
            log.debug("extract order slots by tiny model failed, fallback to rules", e);
        }
        return slots;
    }

    private OrderSlots extractSlotsByRule(String prompt) {
        OrderSlots slots = new OrderSlots();
        String text = prompt == null ? "" : prompt.trim();
        if (text.isBlank()) {
            return slots;
        }

        for (String city : CITY_CANDIDATES) {
            if (text.contains(city)) {
                slots.setCityName(city);
                break;
            }
        }

        Matcher actorMatcher = ACTOR_PATTERN.matcher(text);
        if (actorMatcher.find()) {
            slots.setActor(actorMatcher.group(1));
        }

        Matcher mobileMatcher = MOBILE_PATTERN.matcher(text);
        if (mobileMatcher.find()) {
            slots.setMobile(mobileMatcher.group());
        }

        Matcher priceMatcher = PRICE_PATTERN.matcher(text);
        if (priceMatcher.find()) {
            slots.setTicketCategoryPrice(new BigDecimal(priceMatcher.group(1)));
        }

        Matcher countMatcher = COUNT_PATTERN.matcher(text);
        if (countMatcher.find()) {
            slots.setTicketCount(Integer.parseInt(countMatcher.group(1)));
        }

        Matcher dateMatcher = DATE_PATTERN.matcher(text);
        if (dateMatcher.find()) {
            slots.setShowTime(dateMatcher.group(1).replace("/", "-").replace(".", "-"));
        }

        Matcher idMatcher = ID_NUMBER_PATTERN.matcher(text);
        List<String> ticketUsers = new ArrayList<>();
        while (idMatcher.find()) {
            String id = idMatcher.group();
            if (id != null && id.length() >= 8 && !id.equals(slots.getMobile())) {
                ticketUsers.add(id);
            }
        }
        if (!ticketUsers.isEmpty()) {
            slots.setTicketUserNumberList(ticketUsers);
        }

        return slots;
    }

    private List<String> collectMissingSlots(OrderSlots slots) {
        List<String> missing = new ArrayList<>();
        if (slots.getCityName() == null || slots.getCityName().isBlank()) {
            missing.add("演出城市");
        }
        if (slots.getActor() == null || slots.getActor().isBlank()) {
            missing.add("艺人/节目名");
        }
        if (slots.getMobile() == null || slots.getMobile().isBlank()) {
            missing.add("手机号");
        }
        if (slots.getTicketCategoryPrice() == null) {
            missing.add("票档价位");
        }
        if (slots.getTicketCount() == null || slots.getTicketCount() <= 0) {
            missing.add("购票张数");
        }
        if (slots.getTicketUserNumberList() == null || slots.getTicketUserNumberList().isEmpty()) {
            missing.add("购票人证件号码列表");
        }
        return missing;
    }

    private OrderSlots parseSlots(String slotsJson) {
        try {
            if (slotsJson == null || slotsJson.isBlank()) {
                return new OrderSlots();
            }
            return JSON.parseObject(slotsJson, OrderSlots.class);
        } catch (Exception e) {
            return new OrderSlots();
        }
    }

    private OrderSlots mergeSlots(OrderSlots base, OrderSlots patch) {
        OrderSlots merged = base == null ? new OrderSlots() : base;
        if (patch == null) {
            return merged;
        }
        if (patch.getCityName() != null && !patch.getCityName().isBlank()) {
            merged.setCityName(patch.getCityName().trim());
        }
        if (patch.getActor() != null && !patch.getActor().isBlank()) {
            merged.setActor(patch.getActor().trim());
        }
        if (patch.getShowTime() != null && !patch.getShowTime().isBlank()) {
            merged.setShowTime(patch.getShowTime().trim());
        }
        if (patch.getMobile() != null && !patch.getMobile().isBlank()) {
            merged.setMobile(patch.getMobile().trim());
        }
        if (patch.getTicketCategoryPrice() != null) {
            merged.setTicketCategoryPrice(patch.getTicketCategoryPrice());
        }
        if (patch.getTicketCount() != null && patch.getTicketCount() > 0) {
            merged.setTicketCount(patch.getTicketCount());
        }
        if (patch.getTicketUserNumberList() != null && !patch.getTicketUserNumberList().isEmpty()) {
            merged.setTicketUserNumberList(patch.getTicketUserNumberList());
        }
        return merged;
    }

    private ProgramSearchFunctionDto toProgramSearch(OrderSlots slots) throws ParseException {
        ProgramSearchFunctionDto dto = new ProgramSearchFunctionDto();
        dto.setCityName(slots.getCityName());
        dto.setActor(slots.getActor());
        if (slots.getShowTime() != null && !slots.getShowTime().isBlank()) {
            dto.setShowTime(parseDate(slots.getShowTime()));
        }
        return dto;
    }

    private CreateOrderFunctionDto toCreateOrderDto(OrderSlots slots) throws ParseException {
        CreateOrderFunctionDto dto = new CreateOrderFunctionDto();
        dto.setCityName(slots.getCityName());
        dto.setActor(slots.getActor());
        dto.setMobile(slots.getMobile());
        dto.setTicketCategoryPrice(slots.getTicketCategoryPrice());
        dto.setTicketCount(slots.getTicketCount());
        dto.setTicketUserNumberList(slots.getTicketUserNumberList());
        if (slots.getShowTime() != null && !slots.getShowTime().isBlank()) {
            dto.setShowTime(parseDate(slots.getShowTime()));
        }
        return dto;
    }

    private Date parseDate(String text) throws ParseException {
        String normalized = text.replace("/", "-").replace(".", "-");
        return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(normalized);
    }

    private TicketCategoryVo matchTicketByPrice(List<TicketCategoryVo> ticketCategoryVoList, BigDecimal targetPrice) {
        if (ticketCategoryVoList == null || targetPrice == null) {
            return null;
        }
        for (TicketCategoryVo ticket : ticketCategoryVoList) {
            if (ticket.getPrice() != null && Objects.equals(ticket.getPrice().compareTo(targetPrice), 0)) {
                return ticket;
            }
        }
        return null;
    }

    private boolean isConfirmPrompt(String prompt) {
        String text = prompt == null ? "" : prompt.toLowerCase(Locale.ROOT);
        for (String key : ORDER_CONFIRM_KEYS) {
            if (text.contains(key.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldRetry(String prompt, AiOrderExecutionState executionState) {
        int retry = executionState.getRetryCount() == null ? 0 : executionState.getRetryCount();
        if (retry >= MAX_RETRY) {
            return false;
        }
        if (prompt == null || prompt.isBlank()) {
            return false;
        }
        String text = prompt.toLowerCase(Locale.ROOT);
        return text.contains("继续")
                || text.contains("重试")
                || text.contains("修正")
                || text.contains("补充")
                || text.contains("重新")
                || text.contains("retry");
    }

    private AiOrderExecutionStatus parseState(String state) {
        if (state == null || state.isBlank()) {
            return AiOrderExecutionStatus.INIT;
        }
        try {
            return AiOrderExecutionStatus.valueOf(state.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return AiOrderExecutionStatus.INIT;
        }
    }

    private JSONObject parseJsonObject(String json) {
        try {
            return JSON.parseObject(json);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private String extractJson(String content) {
        if (content == null) {
            return "{}";
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }

    private String md5Base64(String seed) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digest = messageDigest.digest(seed.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            return String.valueOf(seed.hashCode());
        }
    }

    private String safeMessage(Throwable throwable) {
        String message = throwable == null ? "未知异常" : throwable.getMessage();
        return message == null || message.isBlank() ? "未知异常" : message;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isBlank() || "null".equalsIgnoreCase(trimmed)) {
            return null;
        }
        return trimmed;
    }

    private String safeText(String text) {
        return text == null ? "" : text;
    }
}
