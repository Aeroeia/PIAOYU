package com.damai.ai.function;

import cn.hutool.core.collection.CollectionUtil;
import com.damai.ai.function.call.OrderCall;
import com.damai.ai.function.call.ProgramCall;
import com.damai.ai.function.call.TicketCategoryCall;
import com.damai.ai.function.call.UserCall;
import com.damai.ai.function.dto.CreateOrderFunctionDto;
import com.damai.ai.function.dto.ProgramRecommendFunctionDto;
import com.damai.ai.function.dto.ProgramSearchFunctionDto;
import com.damai.dto.ProgramDetailDto;
import com.damai.dto.ProgramOrderCreateDto;
import com.damai.dto.TicketCategoryListByProgramDto;
import com.damai.utils.StringUtil;
import com.damai.vo.*;
import com.damai.vo.result.ProgramDetailResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.damai.constants.DaMaiConstant.ORDER_LIST_ADDRESS;

@Component
@Slf4j
public class AiProgram {
    @Autowired
    private ProgramCall programCall;
    @Autowired
    private TicketCategoryCall ticketCategoryCall;
    @Autowired
    private UserCall userCall;
    @Autowired
    private OrderCall orderCall;
    @Tool(description = "根据地区或者类型查询推荐的节目")
    public List<ProgramSearchVo> selectProgramList(@ToolParam(description = "查询的条件",required = true) ProgramRecommendFunctionDto programRecommendFunctionDto){
        log.info("进入functionCall");
        List<ProgramSearchVo> programSearchVos = programCall.recommondList(programRecommendFunctionDto);
        if (programSearchVos != null) {
            log.info("返回结果数量: {}", programSearchVos.size());
        } else {
            log.info("返回结果为 null");
        }
        return programSearchVos;
    }
    @Tool(description = "根据条件查询节目")
    public List<ProgramSearchVo> search(@ToolParam(description = "查询节目的条件") ProgramSearchFunctionDto programSearchFunctionDto){
        log.info("根据条件查询节目FunctionCall");
        return programCall.search(programSearchFunctionDto);
    }
    @Tool(description = "根据条件查询节目和演唱会的详情")
    public ProgramDetailVo detail(@ToolParam(description = "查询的条件", required = true) ProgramSearchFunctionDto programSearchFunctionDto){
        return selectTicketCategory(programSearchFunctionDto);
    }
    @Tool(description = "根据条件查询节目和演唱会的票档信息")
    public ProgramDetailVo selectTicketCategory(@ToolParam(description = "查询的条件", required = true) ProgramSearchFunctionDto programSearchFunctionDto){
        //复用接口
        List<ProgramSearchVo> programSearchVoList = programCall.search(programSearchFunctionDto);
        if (CollectionUtil.isEmpty(programSearchVoList)) {
            return null;
        }
        //获取详情
        ProgramSearchVo programSearchVo = programSearchVoList.get(0);
        ProgramDetailDto programDetailDto = new ProgramDetailDto();
        programDetailDto.setId(programSearchVo.getId());
        //调用大麦接口获取节目详情
        ProgramDetailResultVo programDetailResultVo = programCall.detail(programDetailDto);
        if (Objects.isNull(programDetailResultVo.getData())) {
            return null;
        }
        ProgramDetailVo programDetailVo = programDetailResultVo.getData();
        //由于详情中设置的飘荡数量不可看 因此再一次查询
        TicketCategoryListByProgramDto ticketCategoryListByProgramDto = new TicketCategoryListByProgramDto();
        ticketCategoryListByProgramDto.setProgramId(programDetailVo.getId());
        List<TicketCategoryDetailVo> ticketCategoryDetailVoList = ticketCategoryCall.selectListByProgram(ticketCategoryListByProgramDto);
        Map<Long, TicketCategoryDetailVo> ticketCategoryDetailMap = ticketCategoryDetailVoList.stream()
                .collect(Collectors.toMap(TicketCategoryDetailVo::getId,
                        ticketCategoryDetailVo -> ticketCategoryDetailVo,
                        (v1, v2) -> v2));
        for (TicketCategoryVo ticketCategoryVo : programDetailVo.getTicketCategoryVoList()) {
            TicketCategoryDetailVo ticketCategoryDetailVo = ticketCategoryDetailMap.get(ticketCategoryVo.getId());
            if (Objects.nonNull(ticketCategoryDetailVo)) {
                ticketCategoryVo.setRemainNumber(ticketCategoryDetailVo.getRemainNumber());
                ticketCategoryVo.setTotalNumber(ticketCategoryDetailVo.getTotalNumber());
            }
        }
        return programDetailVo;
    }
    @Tool(description = "生成用户购买节目的订单，返回订单号")
    public CreateOrderVo createOrder(@ToolParam(description = "查询的条件", required = true) CreateOrderFunctionDto createOrderFunctionDto){
        //查询具体节目and票档
        ProgramSearchFunctionDto programSearchFunctionDto = new ProgramSearchFunctionDto();
        BeanUtils.copyProperties(createOrderFunctionDto, programSearchFunctionDto);
        ProgramDetailVo programDetailVo = selectTicketCategory(programSearchFunctionDto);
        if (Objects.isNull(programDetailVo)) {
            throw new RuntimeException("没有查询到节目，请检查查询条件是否正确");
        }
        //获取用户详情
        UserDetailVo userDetailVo = userCall.userDetail(createOrderFunctionDto.getMobile());
        if (Objects.isNull(userDetailVo)) {
            throw new RuntimeException("用户信息不存在");
        }
        //获取购票人详情
        List<TicketUserVo> ticketUserVoList = userCall.ticketUserList(userDetailVo.getId());
        if (CollectionUtil.isEmpty(ticketUserVoList)) {
            throw new RuntimeException("购票人信息不存在");
        }
        List<TicketUserVo> ticketUserVoFilterList = new ArrayList<>();
        //校验购票人信息是否对的上
        for (final TicketUserVo ticketUserVo : ticketUserVoList) {
            for (final String number : createOrderFunctionDto.getTicketUserNumberList()) {
                String ticketUserNumberFirst = StringUtil.getFirstN(ticketUserVo.getIdNumber(),4);
                String ticketUserNumberLast = StringUtil.getLastN(ticketUserVo.getIdNumber(),4);

                String paramNumberFirst = StringUtil.getFirstN(number,4);
                String paramNumberLast = StringUtil.getLastN(number,4);

                if (ticketUserNumberFirst.equals(paramNumberFirst) && ticketUserNumberLast.equals(paramNumberLast)) {
                    ticketUserVoFilterList.add(ticketUserVo);
                }
            }
        }
        if (ticketUserVoFilterList.size() != createOrderFunctionDto.getTicketUserNumberList().size()) {
            throw new RuntimeException("购票人信息不完整，请检查购票人信息是否正确");
        }
        Long ticketCategoryId = null;
        //通过金额比对确定票档
        for (final TicketCategoryVo ticketCategoryVo : programDetailVo.getTicketCategoryVoList()) {
            if (createOrderFunctionDto.getTicketCategoryPrice().compareTo(ticketCategoryVo.getPrice()) == 0) {
                ticketCategoryId = ticketCategoryVo.getId();
                break;
            }
        }
        if (Objects.isNull(ticketCategoryId)) {
            throw new RuntimeException("没有查询到对应的票档信息");
        }
        //构造购票DTO
        ProgramOrderCreateDto programOrderCreateDto = new ProgramOrderCreateDto();
        programOrderCreateDto.setProgramId(programDetailVo.getId());
        programOrderCreateDto.setUserId(userDetailVo.getId());
        programOrderCreateDto.setTicketUserIdList(ticketUserVoFilterList.stream().map(TicketUserVo::getId).collect(Collectors.toList()));
        programOrderCreateDto.setTicketCategoryId(ticketCategoryId);
        programOrderCreateDto.setTicketCount(createOrderFunctionDto.getTicketCount());
        //发起http请求下单
        String orderNumber = orderCall.createOrder(programOrderCreateDto);
        CreateOrderVo createOrderVo = new CreateOrderVo();
        createOrderVo.setOrderNumber(orderNumber);
        createOrderVo.setOrderListAddress(ORDER_LIST_ADDRESS);
        return createOrderVo;
    }
}
