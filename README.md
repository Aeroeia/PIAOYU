# 节目服务

## 高性能节目详情展示功能

```
@Operation(summary  = "查询详情(根据id)")
@PostMapping(value = "/detail")
public ApiResponse<ProgramVo> getDetail(@Valid @RequestBody ProgramGetDto programGetDto) {
    return ApiResponse.ok(programService.detail(programGetDto));
}

@Operation(summary  = "查询详情V1(根据id)")
@PostMapping(value = "/detail/v1")
public ApiResponse<ProgramVo> getDetailV1(@Valid @RequestBody ProgramGetDto programGetDto) {
    return ApiResponse.ok(programService.detailV1(programGetDto));
}

@Operation(summary  = "查询详情V2(根据id)")
@PostMapping(value = "/detail/v2")
public ApiResponse<ProgramVo> getDetailV2(@Valid @RequestBody ProgramGetDto programGetDto) {
    return ApiResponse.ok(programService.detailV2(programGetDto));
}
```

### 接口执行流程

**1. Controller层入口**

```java
@Operation(summary  = "查询详情V2(根据id)")
@PostMapping(value = "/detail/v2")
public ApiResponse<ProgramVo> getDetailV2(@Valid @RequestBody ProgramGetDto programGetDto) {
    return ApiResponse.ok(programService.detailV2(programGetDto));
}
```

**2. Service层处理流程**

从代码分析，`detailV2`方法的执行流程如下：

**步骤1：获取节目演出时间信息**

```java
ProgramShowTime programShowTime =
        programShowTimeService.selectProgramShowTimeByProgramIdMultipleCache(programGetDto.getId());
```


这个方法采用了三级缓存策略：

1. 首先查询本地缓存[LocalCacheProgramShowTime](file:///Users/aer/IdeaProjects/damai/damai-server/damai-program-service/src/main/java/com/damai/service/cache/local/LocalCacheProgramShowTime.java#L32-L75)
2. 本地缓存未命中则查询Redis缓存
3. Redis缓存未命中则通过分布式锁查询数据库并写入缓存 ==分布式锁是为了缓解高并发数据库压力==

**步骤2：获取节目基本信息**

```java
ProgramVo programVo = programService.getByIdMultipleCache(programGetDto.getId(),programShowTime.getShowTime());
```


同样采用三级缓存策略：

1. 首先查询本地缓存[LocalCacheProgram](file:///Users/aer/IdeaProjects/damai/damai-server/damai-program-service/src/main/java/com/damai/service/cache/local/LocalCacheProgram.java#L31-L75)
2. 本地缓存未命中则查询Redis缓存
3. Redis缓存未命中则通过分布式锁查询数据库并写入缓存

**步骤3：设置演出时间信息**

```java
programVo.setShowTime(programShowTime.getShowTime());
programVo.setShowDayTime(programShowTime.getShowDayTime());
programVo.setShowWeekTime(programShowTime.getShowWeekTime());
```

**步骤4：获取节目分组信息**

```java
ProgramGroupVo programGroupVo = programService.getProgramGroupMultipleCache(programVo.getProgramGroupId());
programVo.setProgramGroupVo(programGroupVo);
```


同样采用三级缓存策略：

1. 首先查询本地缓存[LocalCacheProgramGroup](file:///Users/aer/IdeaProjects/damai/damai-server/damai-program-service/src/main/java/com/damai/service/cache/local/LocalCacheProgramGroup.java#L31-L72)
2. 本地缓存未命中则查询Redis缓存
3. Redis缓存未命中则通过分布式锁查询数据库并写入缓存

**步骤5：预加载相关数据**

```java
preloadTicketUserList(programVo.getHighHeat());
preloadAccountOrderCount(programVo.getId());
```


这两个方法会根据条件异步预加载购票人列表和账户订单计数，提升用户体验。

**步骤6：获取节目分类信息**

```java
ProgramCategory programCategory = getProgramCategoryMultipleCache(programVo.getProgramCategoryId());
if (Objects.nonNull(programCategory)) {
    programVo.setProgramCategoryName(programCategory.getName());
}
ProgramCategory parentProgramCategory = getProgramCategoryMultipleCache(programVo.getParentProgramCategoryId());
if (Objects.nonNull(parentProgramCategory)) {
    programVo.setParentProgramCategoryName(parentProgramCategory.getName());
}
```

**步骤7：获取票档信息**

```java
List<TicketCategoryVo> ticketCategoryVoList = ticketCategoryService
        .selectTicketCategoryListByProgramIdMultipleCache(programVo.getId(),programShowTime.getShowTime());
programVo.setTicketCategoryVoList(ticketCategoryVoList);
```

------


### 缓存策略总结

整个流程采用了多级缓存架构：

1. **本地缓存（Caffeine）**：提供最快的访问速度，减轻Redis压力
2. **Redis缓存**：提供分布式缓存能力，确保多实例间数据一致性
3. **数据库**：作为最终数据来源，当缓存都未命中时查询

这种多级缓存策略可以显著提升系统性能，特别是在高并发场景下，能够有效减少数据库访问压力。

**首先添加分布式锁，锁的类型为读锁，写锁是在节目添加接口。使用读写锁可以减少锁的竞争。当其他用户进行查看节目详情时，都是读锁，读锁和读锁之间不会引起锁的竞争**

**获取详情时候加读锁，添加节目时候加的写锁，**
**下面的RLock是缓存重建加锁的**

------

### 懒汉式单例

#### 懒汉式单例模式中的双重检查锁

```java
public class Singleton {
    private static volatile Singleton instance;
    
    public static Singleton getInstance() {
        // 第一次检查，避免不必要的同步
        if (instance == null) {
            synchronized (Singleton.class) {
                // 第二次检查，确保只创建一个实例
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```


#### 在代码中的应用

在您提到的代码中，双重检查锁的思想体现在：

1. **第一次检查**：检查Redis缓存中是否存在数据

```java
ProgramVo programVo = redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM, programId), ProgramVo.class);
if (Objects.nonNull(programVo)) {
    return programVo;  // 缓存命中，直接返回
}
```


2. **获取锁**：如果缓存未命中，则获取分布式锁

```java
RLock lock = serviceLockTool.getLock(LockType.Reentrant, GET_PROGRAM_LOCK, new String[]{String.valueOf(programId)});
lock.lock();
```


3. **第二次检查**：获取锁后再检查一次缓存

```java
try {
    // 再次检查缓存，防止其他线程已经重建了缓存
    return redisCache.get(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM,programId)
            ,ProgramVo.class,
            () -> createProgramVo(programId)
            ,expireTime,
            timeUnit);
}finally {
    lock.unlock();
}
```

#### 与单例模式的相似性

这种模式与懒汉式单例的双重检查锁非常相似：

- 都是先检查条件（缓存是否存在/实例是否已创建）
- 都是在条件不满足时获取锁
- 都是在获取锁后再次检查条件
- 都是为了避免不必要的同步开销

这种设计思想在高并发系统中非常常见，是一种很好的性能优化手段。

------

### 防止大量缓存同时失效问题

![image-20250926223900936](assets/image-20250926223900936.png)

将节目演出时间与当前时间相隔时间作为缓存过期时间

------

### 为什么将节目多个部分分开存储到Redis

![查看节目详情.png](assets/1721723736110-45140271-c2fc-459c-8f9e-7457bc7022fb.png)

1. **数据更新频率不同**
   - 节目基本信息（如标题、演员等）很少变化，可以长时间缓存
   - 票档信息和座位信息可能频繁更新（如余票数量），需要更短的缓存时间或独立更新
   - 演出时间等信息可能需要根据实时情况调整

2. **数据一致性要求不同**
   - 核心节目信息相对稳定，即使缓存稍有延迟也问题不大
   - 票档余票信息需要相对实时，需要独立管理和更新策略
   - 分开存储可以针对不同数据制定不同的缓存失效策略

3. **性能优化考虑**
   - 当只需要获取节目基本信息时，不需要加载完整的票档信息，减少网络传输和内存占用
   - 对于高并发场景，可以只更新需要变化的部分，而不是整个对象
   - 减少Redis中序列化/反序列化的数据量，提高响应速度

4. **缓存命中率优化**
   - 基础节目信息访问频率远高于票档信息
   - 分离存储可以提高基础信息的缓存命中率，因为不需要因为票档信息的变更而清除整个节目对象缓存

5. **系统解耦**
   - 不同服务可能只需要节目信息的不同部分
   - 分离存储有助于服务间的解耦，每个服务只需关注自己需要的部分

在[ProgramService.getDetail](file:///Users/aer/IdeaProjects/damai/damai-server/damai-program-service/src/main/java/com/damai/service/ProgramService.java#L443-L472)方法中，我们可以看到：

- 首先通过`programService.getById`获取节目基本信息
- 然后单独获取节目演出时间信息
- 接着独立获取节目分组信息
- 再单独获取节目分类信息
- 最后获取票档信息列表

这种设计使得每种数据都可以有不同的缓存策略和更新机制，提高了系统的灵活性和性能。

------

### 锁优化

![image-20250926234636351](assets/image-20250926234636351.png)

现在除了第一次请求外，其余的请求确实是能从缓存中获得了，但所有的请求还是要挨个去竞争锁的，每个请求依旧还是有获取锁，加锁以及再解锁的过程，还是要挨个的串行处理请求的，依旧不能实现并发的处理

**假设查看同一个节目的并发请求有100w，假设Redis处理锁的请求执行1毫秒就完成，那么等到最后一个请求，还是要等待999999毫秒，约等于16分钟左右**

改为trylock，1s后得不到锁直接走下面流程

![image-20250926235136302](assets/image-20250926235136302.png)

**存在一个问题：如果发生网络问题，一秒内没将数据存到redis中，还是会有大量数据打入数据库导致缓存击穿，但高并发与绝对安全不可兼得，要作出取舍**

![Redis缓存+双重检测+tryLock.png](assets/1712644645064-cfa44494-ce92-4eb6-a58c-88d61d545fe1.png)

------

## 用户选择节目座位

### Redis缓存结构

![image-20250929193849505](assets/image-20250929193849505.png)

如果采用上面这种Hash结构，由于redis是单线程模型，同一节目下如果不同用户抢不同票档的票会发生竞争关系，比如两个用户分别同时抢看台和内场的票，就会发生内场用户需要等待看台用户抢完或者反之 集群下也会如此

**一个 Hash 存所有票档** → 设计本身没问题，语义清晰、管理方便。

**问题只会在高并发场景下出现** → 因为所有操作都集中在同一个 Key 上：

1. Redis 是单线程 → 一个 Key 的操作只能排队执行。
2. Redis Cluster 分片是 **按 Key 粒度** → 一个 Key 必须落在一个槽位上，不能拆到多个分片。
3. 所以这个热点 Key 会成为性能瓶颈，吞吐量打不满整个集群。

**优化后**

![image-20250929195435067](assets/image-20250929195435067.png)

1. 解决不同票档竞争关系
2. 票数压力 如果key只用节目id，几万张票存入一个hash需要过滤出一个票档的 压力很大

**再优化**

![image-20250929205051041](assets/image-20250929205051041.png)

根据票的状态进行分开存放，==但还是在一个redis实例中(保证lua脚本读)==

```java
 public List<SeatVo> getSeatVoListByCacheResolution(Long programId,Long ticketCategoryId){
        List<String> keys = new ArrayList<>(4);
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_NO_SOLD_RESOLUTION_HASH,
                programId, ticketCategoryId).getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_LOCK_RESOLUTION_HASH,
                programId, ticketCategoryId).getRelKey());
        keys.add(RedisKeyBuild.createRedisKey(RedisKeyManage.PROGRAM_SEAT_SOLD_RESOLUTION_HASH,
                programId, ticketCategoryId).getRelKey());
        return programSeatCacheData.getData(keys, new String[]{});
    }
```

------

## 解决高并发下购票压力 

![购票流程V1.png](assets/1723690965796-2001f2f8-b7d2-4307-bdba-9355c89b4693.png)

------

### 购票前组合验证

用户购票验证逻辑的类型为`program_order_create_check`，验证的逻辑有

-  验证参数 包含用户id是否唯一、按照是否手动选座进行校验参数是否非空 

```java
  @Override
    protected void execute(final ProgramOrderCreateDto programOrderCreateDto) {
        List<SeatDto> seatDtoList = programOrderCreateDto.getSeatDtoList();
        List<Long> ticketUserIdList = programOrderCreateDto.getTicketUserIdList();
        Map<Long, List<Long>> ticketUserIdMap = 
                ticketUserIdList.stream().collect(Collectors.groupingBy(ticketUserId -> ticketUserId));
        for (List<Long> value : ticketUserIdMap.values()) {
            if (value.size() > 1) {
                throw new DaMaiFrameException(BaseCode.TICKET_USER_ID_REPEAT);
            }
        }
        if (CollectionUtil.isNotEmpty(seatDtoList)) {
            if (seatDtoList.size() != programOrderCreateDto.getTicketUserIdList().size()) {
                throw new DaMaiFrameException(BaseCode.TICKET_USER_COUNT_UNEQUAL_SEAT_COUNT);
            }
            for (SeatDto seatDto : seatDtoList) {
                if (Objects.isNull(seatDto.getId())) {
                    throw new DaMaiFrameException(BaseCode.SEAT_ID_EMPTY);
                }
                if (Objects.isNull(seatDto.getTicketCategoryId())) {
                    throw new DaMaiFrameException(BaseCode.SEAT_TICKET_CATEGORY_ID_EMPTY);
                }
                if (Objects.isNull(seatDto.getRowCode())) {
                    throw new DaMaiFrameException(BaseCode.SEAT_ROW_CODE_EMPTY);
                }
                if (Objects.isNull(seatDto.getColCode())) {
                    throw new DaMaiFrameException(BaseCode.SEAT_COL_CODE_EMPTY);
                }
                if (Objects.isNull(seatDto.getPrice())) {
                    throw new DaMaiFrameException(BaseCode.SEAT_PRICE_EMPTY);
                }
            }
        }else {
            if (Objects.isNull(programOrderCreateDto.getTicketCategoryId())) {
                throw new DaMaiFrameException(BaseCode.TICKET_CATEGORY_NOT_EXIST);
            }
            if (Objects.isNull(programOrderCreateDto.getTicketCount())) {
                throw new DaMaiFrameException(BaseCode.TICKET_COUNT_NOT_EXIST);
            }
            if (programOrderCreateDto.getTicketCount() <= 0) {
                throw new DaMaiFrameException(BaseCode.TICKET_COUNT_ERROR);
            }
        }
    }
```

- 校验是否可以选座以及购票数量是否上限

```java
 @Override
    protected void execute(final ProgramOrderCreateDto programOrderCreateDto) {
        ProgramGetDto programGetDto = new ProgramGetDto();
        programGetDto.setId(programOrderCreateDto.getProgramId());
        ProgramVo programVo = programService.detailV2(programGetDto);
        if (programVo.getPermitChooseSeat().equals(BusinessStatus.NO.getCode())) {
            if (Objects.nonNull(programOrderCreateDto.getSeatDtoList())) {
                throw new DaMaiFrameException(BaseCode.PROGRAM_NOT_ALLOW_CHOOSE_SEAT);
            }
        }
        Integer seatCount = Optional.ofNullable(programOrderCreateDto.getSeatDtoList()).map(List::size).orElse(0);
        Integer ticketCount = Optional.ofNullable(programOrderCreateDto.getTicketCount()).orElse(0);
        if (seatCount > programVo.getPerOrderLimitPurchaseCount() || ticketCount > programVo.getPerOrderLimitPurchaseCount()) {
            throw new DaMaiFrameException(BaseCode.PER_ORDER_PURCHASE_COUNT_OVER_LIMIT);
        }
    }
```

-  验证用户是否存在 检查已购票数+购票数是否超出上限 

![image-20251228153424906](assets/image-20251228153424906.png)

------

### 基本流程

1. 执行组合验证

![image-20251228151804330](assets/image-20251228151804330.png)

2. 查询演出时间
3. 根据传入的票档id进行查询票档信息
4. 根据对应票档查询未售卖座位

![image-20251002012323127](assets/image-20251002012323127.png)

5. 查询余票数量

![image-20251002013554288](assets/image-20251002013554288.png)

6. 判断每个票档的购买票数是否大于余票

![image-20251002014754869](assets/image-20251002014754869.png)

7. 判断前端传入价格是否和真实价格对应的上

![image-20251002014851025](assets/image-20251002014851025.png)

8. 进行扣减库存、更改座位状态 通过lua脚本

```lua
-- 解析传入的参数，将JSON字符串转换为Lua表
-- ARGV[1]: 票档余票数量更新列表
-- ARGV[2]: 需要删除的座位列表
-- ARGV[3]: 需要添加的座位数据列表
local ticket_category_list = cjson.decode(ARGV[1])
local del_seat_list = cjson.decode(ARGV[2])
local add_seat_data_list = cjson.decode(ARGV[3])

-- 遍历票档列表，更新每个票档的余票数量
for index,increase_data in ipairs(ticket_category_list) do
    -- 获取余票数量hash键
    local program_ticket_remain_number_hash_key = increase_data.programTicketRemainNumberHashKey
    -- 获取票档ID
    local ticket_category_id = increase_data.ticketCategoryId
    -- 获取需要增加的票数（可以为负数表示减少）
    local increase_count = increase_data.count
    -- 原子性地增加或减少余票数量
    redis.call('HINCRBY',program_ticket_remain_number_hash_key,ticket_category_id,increase_count)
end

-- 遍历需要删除的座位列表，从Redis中删除座位数据
for index, seat in pairs(del_seat_list) do
    -- 获取座位hash键（需要从中删除座位）
    local seat_hash_key_del = seat.seatHashKeyDel
    -- 获取需要删除的座位ID列表
    local seat_id_list = seat.seatIdList
    -- 从hash中删除指定的座位
    redis.call('HDEL',seat_hash_key_del,unpack(seat_id_list))
end

-- 遍历需要添加的座位数据列表，向Redis中添加座位数据
for index, seat in pairs(add_seat_data_list) do
    -- 获取座位hash键（需要向其中添加座位）
    local seat_hash_key_add = seat.seatHashKeyAdd
    -- 获取座位数据列表
    local seat_data_list = seat.seatDataList
    -- 将座位数据批量设置到hash中
    redis.call('HMSET',seat_hash_key_add,unpack(seat_data_list))
end
```

![image-20251002175420114](assets/image-20251002175420114.png)

![image-20251110100456608](assets/image-20251110100456608.png)

感觉可以将票档余票数量结构改成==redis中key只包含节目id hash中存不同票档的余票数量==

9. 创建主订单和购票人订单(order&order_ticket_user)

![image-20251002225006264](assets/image-20251002225006264.png)

10. 远程调用rpc创建订单

![image-20251002225250360](assets/image-20251002225250360.png)

![image-20251228151738514](assets/image-20251228151738514.png)

11. 如果订单生成失败 调用之前扣减库存的方法进行回滚

![image-20251002235230435](assets/image-20251002235230435.png)

12. 发送延迟消息队列取消订单

![image-20251003030347205](assets/image-20251003030347205.png)

13. 恢复缓存余票和座位状态

![image-20251003030453650](assets/image-20251003030453650.png)

------

### 锁优化  V2

- key：节目+票档id 使得可以多redis实例并发
- 加入本地锁 缓解redis压力
- 由于网络可能有差异 先进入本地锁的不一定先竞争到分布式锁

![用户购票分布式锁V2(加入本地锁).png](assets/1723690792208-2cd0aedb-e544-4b8b-8746-bcfb9c10c4c9.png)

![本地锁+分布式锁公平性.png](assets/1723690824820-4361ae1c-6f08-4e5f-a975-119afe7b3e84.png)

![image-20251006143033740](assets/image-20251006143033740.png)

------

### 无锁化 V3

前面做的事情主要是==三重验证==、加==幂等锁==给==不同票档加本地锁==、==计算不同票档所需票==、给lua脚本中key加标识看是否为手动选座，==手动选座则传入dto中的选座信息==

Lua脚本执行流程图

![bdc1ced9-4238-4fb0-a844-1e8101dc0415](assets/bdc1ced9-4238-4fb0-a844-1e8101dc0415.png)

------

### 调用Kafka异步创建订单 V4

![用户购票v4.png](assets/1720954650416-a9595510-d817-402b-a1e7-aacf2f9ea18f.png)

![image-20251228151658854](assets/image-20251228151658854.png)

![image-20251007034622425](assets/image-20251007034622425.png)

==lathch的作用是等待kafka发送消息完毕==

**消费者**

```java
@Slf4j
@AllArgsConstructor
@Component
public class CreateOrderConsumer {
    
    @Autowired
    private OrderService orderService;
    
    public static Long MESSAGE_DELAY_TIME = 5000L;
    
    @KafkaListener(topics = {SPRING_INJECT_PREFIX_DISTINCTION_NAME+"-"+"${spring.kafka.topic:create_order}"})
    public void consumerOrderMessage(ConsumerRecord<String,String> consumerRecord){
        try {
            Optional.ofNullable(consumerRecord.value()).map(String::valueOf).ifPresent(value -> {
                
                OrderCreateDto orderCreateDto = JSON.parseObject(value, OrderCreateDto.class);
                
                long createOrderTimeTimestamp = orderCreateDto.getCreateOrderTime().getTime();
                
                long currentTimeTimestamp = System.currentTimeMillis();
                
                long delayTime = currentTimeTimestamp - createOrderTimeTimestamp;
                
                log.info("消费到kafka的创建订单消息 消息体: {} 延迟时间 : {} 毫秒",value,delayTime);
                //如果消费到消息时，延迟时间超过了5s，那么此订单丢弃，将库存回滚回去
                if (currentTimeTimestamp - createOrderTimeTimestamp > MESSAGE_DELAY_TIME) {
                    log.info("消费到kafka的创建订单消息延迟时间大于了 {} 毫秒 此订单消息被丢弃 订单号 : {}",
                            delayTime,orderCreateDto.getOrderNumber());
                    Map<Long, List<OrderTicketUserCreateDto>> orderTicketUserSeatList =
                            orderCreateDto.getOrderTicketUserCreateDtoList().stream().collect(Collectors.groupingBy(OrderTicketUserCreateDto::getTicketCategoryId));
                    Map<Long,List<Long>> seatMap = new HashMap<>(orderTicketUserSeatList.size());
                    orderTicketUserSeatList.forEach((k,v) -> {
                        seatMap.put(k,v.stream().map(OrderTicketUserCreateDto::getSeatId).collect(Collectors.toList()));
                    });
                    //数据恢复
                    orderService.updateProgramRelatedDataMq(orderCreateDto.getProgramId(),seatMap, OrderStatus.CANCEL);
                }else {
                    String orderNumber = orderService.createMq(orderCreateDto);
                    log.info("消费到kafka的创建订单消息 创建订单成功 订单号 : {}",orderNumber);
                }
            });
        }catch (Exception e) {
            log.error("处理消费到kafka的创建订单消息失败 error",e);
        }
    }
}
```

![image-20251009162933876](assets/image-20251009162933876.png)

这里将订单编号放入redis是为了==虽然第2步返回了订单编号，但由于异步场景，不一定真正的入库了，所以前端要有个定时任务，不断的去轮训订单是否真的存在，如果查询到了，才说明订单是真正的创建成功==
前端服务在轮训时，能更快的查询到。因为redis的性能要比数据库强太多，所以让前端服务直接去redis查询就可以了，数据也不用复杂，订单编号就可以。但是不能一直存放，要设置一个过期时间，这里设置了1分钟

![image-20251009163223981](assets/image-20251009163223981.png)

------

# 订单服务

## 远程支付服务

### 支付流程

![支付流程.png](assets/1723691350784-6d8cc0ea-fbe9-4ed7-b221-441fdf9988e4.png)

------

### 支付页面

![image-20251008180420986](assets/image-20251008180420986.png)

![image-20251008180444510](assets/image-20251008180444510.png)

**返回一个支付宝的html页面**
![image-20251008183557242](assets/image-20251008183557242.png)

==这个界面询问是否继续浏览器付款 随后将返回的表单写入页面会执行自动提交 然后跳转到支付宝的页面，后续操作基于支付宝==

```html
<!-- 商户生成的表单 -->
<form name="punchout_form" method="post" action="https://openapi.alipay.com/gateway.do">
    <!-- 各种参数字段 -->
    <input type="hidden" name="biz_content" value="..."/>
    <!-- 更多隐藏字段 -->
    <input type="submit" value="立即支付" style="display:none"/>
</form>
<!-- 自动提交脚本 -->
<script>document.forms[0].submit();</script>
```

支付成功后

==主动查询方案==

```
前端 paySuccess.vue (页面加载)
        ↓
调用 payCheckApi (请求订单支付状态检查)
        ↓
OrderController.payCheck (订单服务) 更新订单状态
        ↓
PayClient.tradeCheck (调用支付服务) 
        ↓
PayController.tradeCheck (支付服务) 更新pay_bill状态
        ↓
调用支付宝API查询实际支付状态
```

==支付宝异步回调方案==

```
支付宝服务器
      ↓ (异步回调)
OrderController.alipayNotify (订单服务) 更新订单状态
      ↓ (调用支付服务)
PayController.notify (支付服务) 更新pay_bill状态
      ↓
更新支付账单状态
```

支付页面中 点击"继续继续浏览器付款"后页面就跳转到支付宝页面由支付宝管控 当支付成功才跳转回成功页面 还有就是我查看了paysuccess源码 #paySuccess.vue 35-51 这里即使订单状态没有显示为成功也仍然显示支付成功字样

------

## 为何订单这个重要业务也能异步进行？

创建订单过程是加锁执行的 因此如果不尽快处理就会导致阻塞很久 创建订单业务一般都要求采用同步是因为要返回给前端==订单号== 后续要==根据这个订单号进行支付操作== ==要保证订单在数据库存在== 

**该业务进行了以下处理使得订单创建业务也可以异步进行**

- 在节目服务根据基因法创建订单编号，因此能直接返回给前端，异步创建订单，分库分表把订单编号作为分片键
- 通过Countdownlatch等待kafka发送消息完成，若发送失败则回滚余票、座位状态并抛出异常
- [确保了订单创建成功后才能进行支付](#Kafka异步消费订单)

------

### Kafka异步消费订单

创建订单是==异步==的，因此前端需要==轮询查看订单是否创建成功==(Redis)决定是否给用户显示支付界面,设置的是轮询5秒，因此kafka消费方如果接收到的是超过5s的消息则丢弃，回滚redis中的余票和座位状态

- 消息超过5s丢弃并回滚余票和座位状态

![image-20251013000552043](assets/image-20251013000552043.png)

- 订单服务消费kafka消息创建订单成功在redis设置成功标识 前端跳转支付页面
  - 当getOrderCache返回的response.data不为空时，表示订单已成功创建
    停止轮询，关闭loading弹框
  - 跳转到支付页面：router.replace({path:'/order/payMethod',state:{'orderNumber':orderNumberCache.value}})

![image-20251013000753362](assets/image-20251013000753362.png)

### 基因法

基因法的userId%tablecount 是==根据表数量取模== 定位库时取这后几位基因定位 定位表时直接对表数量取模 由于表数量是2^n 所以基因位相同就可以保证定位到同一表 所以最终同一用户下订单能定位同一库表

- 基因生成 `userId % tableCount`

```java
// SnowflakeIdGenerator.getOrderNumber 方法
public synchronized long getOrderNumber(long userId,long tableCount) {
    long timestamp = getBase();
    long sequenceShift = log2N(tableCount);
    return ((timestamp - BASIS_TIME) << timestampLeftShift)
            | (datacenterId << datacenterIdShift)
            | (workerId << workerIdShift)
            | (sequence << sequenceShift)
            | (userId % tableCount); // 这就是基因部分
}
```

-  数据库定位：提取基因位

```java
    public long calculateDatabaseIndex(Integer databaseCount, Long splicingKey, Integer tableCount) {
        // 将分片键转为二进制字符串（如 123 → "1111011"）
        String splicingKeyBinary = Long.toBinaryString(splicingKey);

        // 计算“基因”长度：log2(表分片数)，即需要多少位来表示所有表
        long replacementLength = log2N(tableCount);

        // 从二进制串末尾截取“基因段”（低位）
        String geneBinaryStr = splicingKeyBinary.substring(
                splicingKeyBinary.length() - (int) replacementLength);

        // 若基因段非空
        if (StringUtil.isNotEmpty(geneBinaryStr)) {
            // ❗当前实现：使用字符串哈希（存在跨JVM不一致风险）
            int h;
            int geneOptimizeHashCode = (h = geneBinaryStr.hashCode()) ^ (h >>> 16);
            // 使用位运算取模（要求 databaseCount 是2的幂）
            return (databaseCount - 1) & geneOptimizeHashCode;
        }

        // 基因段为空，抛异常（理论上不应发生）
        throw new DaMaiFrameException(BaseCode.NOT_FOUND_GENE);
    }
```

- 表定位：直接取模

```java
// TableOrderComplexGeneArithmetic.doSharding 方法
@Override
public Collection<String> doSharding(Collection<String> allActualSplitTableNames, 
                                    ComplexKeysShardingValue<Long> complexKeysShardingValue) {
    // ...
    if (Objects.nonNull(value)) {
        // 直接使用 value 与 (shardingCount - 1) 进行位运算得到表索引
        actualTableNames.add(logicTableName + "_" + ((shardingCount - 1) & value));
        return actualTableNames;
    }
    // ...
}
```

------

# 操作记录解决一致性问题

### 1. Redis中新增记录表

==在创建订单扣减库存的同时在Redis中新增一条记录==

![image-20251025153858418](assets/image-20251025153858418.png)

- 记录标识：通过id生成器生成的uid
- 记录类型：分为扣减库存、恢复余票、座位状态修改
- 记录数据拼接操作在lua中进行

![image-20251106002049655](assets/image-20251106002049655.png)

#### Hash结构

![image-20251025154503130](assets/image-20251025154503130.png)

- 大key(32)为节目id
- Hash中key为记录类型-记录id-下单用户id

------

### 2. MySql中新增记录

#### **1. 下单时，在发送Mq创建订单前插入一条记录**

- 定时任务可以直接取这个表中存在的节目进行对账而不是全部取出来对比

![image-20251228152031268](assets/image-20251228152031268.png)

#### **2. 收到创建订单消息后直接同步节目服务数据库中余票**

![image-20251228152044610](assets/image-20251228152044610.png)

#### 3. 创建订单同时新增记录

- 每个购票人记录中关联的是同一个记录id 

![image-20251228152056863](assets/image-20251228152056863.png)

![image-20251028020033661](assets/image-20251028020033661.png)

![image-20251028020323227](assets/image-20251028020323227.png)

#### 4. 在节目订单这张表加一个记录

- 对账使用

![image-20251228152111572](assets/image-20251228152111572.png)

#### 5. 支付/退款回调成功会新增记录

![image-20251228152122236](assets/image-20251228152122236.png)

------

### 3. 对比记录

#### 3.1 查询要对账的节目

==这里使用到前面发送kafka前插入的节目数据==

![image-20251228152132921](assets/image-20251228152132921.png)

- 定时任务定时对账
- 通过rpc调用节目服务查找三分钟前的记录节目
- 查前三分钟是为了防止节目服务发送kafka但是消息还未被消费的记录被读取

![image-20251228152141379](assets/image-20251228152141379.png)

#### 3.2 进行对账

- 通过节目id查询Redis下的记录用来双向对账
- 以Redis为标准对账
  - 将Redis中的数据转为(IdentifyId_userId,recordType)键值对
  - 根据这个dentifyId_userId查询数据库中订单人记录表并转为(recordType,value)键值对
  - 遍历记录将Redis和Mysql中的记录进行对比
    - 两个都没有数据--==无需对账 正常==
    - Redis有而Mysql没有--Kafka消息丢失 
    - Redis没有而Mysql有--Redis消息丢失
    - 二者都有数据

- 以Mysql为标准对账
  - 根据节目id查询出购票人订单记录
  - 进行对比


==比对的是同一identifyId下操作记录是否相同==

#### 3.3 数据不一致返回结果

**Redis缺失记录为例**

```json
[
    {
        "examinationRecordTypeResultList": [
            {
                "examinationSeatResult": {
                    "dbStandardStatisticCount": 0,
                    "needToDbSeatRecordList": [

                    ],
                    "needToRedisSeatRecordList": [
                        {
                            "createTime": 1744890121000,
                            "createType": 1,
                            "editTime": 1745233944000,
                            "id": 988036713682436098,
                            "identifierId": 988037984992780288,
                            "orderNumber": 1912833918217224202,
                            "orderPrice": 380,
                            "programId": 32,
                            "reconciliationStatus": 1,
                            "recordTypeCode": 0,
                            "recordTypeValue": "changeStatus",
                            "seatId": 752,
                            "seatInfo": "1排2列",
                            "status": 1,
                            "ticketCategoryId": 34,
                            "ticketUserId": 927653802827100032,
                            "ticketUserOrderId": 988036713682436097,
                            "userId": 927653802827104258
                        }
                    ],
                    "redisStandardStatisticCount": 0
                },
                "recordTypeCode": 0,
                "recordTypeValue": "changeStatus"
            }
        ],
        "identifierId": "988037984992780288",
        "userId": "927653802827104258"
    }
]
```

返回结果是同一订单下(全程使用的是同一记录id),缺失的记录(扣减、状态变更、订单取消)

------

## 缓存延迟双删

缓存一致性问题发生在 第一次读取为空时在数据库读取并写入缓存 但是这时候发生网络问题写入缓存慢了 恰好这时候有mysql写入操作更新缓存 但由于这时候缓存为空加上之前读取到的是旧值导致网络恢复时写入Redis的也是旧值 这个问题可以通过延迟双删来解决

https://www.bilibili.com/video/BV1EyXQYJEF5/?spm_id_from=333.1387.search.video_card.click&vd_source=fd0b7243ae5b04e4d46b23923b639f5b

------

## 总结

**1. redis扣减库存成功但是写入记录失败 ~~mysql扣减库存也成功了 但是创建订单和写入记录也失败了 就导致空扣了一票 怎么解决的~~**

> 在售票完成后,比对订单、账单数量和余票是否一致,不一致则放

**2. 怎么做操作记录的 怎么保证数据一致性的**

**创建记录:** 

> 1. 在下单的时候扣减完Redis余票,==更改完位置状态后会在Redis中新增==一条扣减余票的操作记录 记录包含了订单的信息以及扣减前后数量  
> 2. 然后Kafka发送创建订单消息 创建订单前会==先将余票和座位信息同步到数据库== 创建订单后在Mysql中新增对应购票订单人的操作记录 
> 3. 支付成功/退款 也会在==Redis和Mysql新增记录==

**对账:**

> 1. Redis Mysql都没数据: 有可能出现在Redis中余票扣成功了但是记录没写宕机同时主从也同步了这一数据 可能会导致Redis扣票了Mysql没扣但==没法感知导致少卖==
>    - 我的想法是==恰赶上Redis宕机时报错的话 然后删除Redis新主节点的缓存==重新在Mysql同步
>    - 如果哨兵已经切换节点了感知不到那只能少卖了 问题不大,售票结束对一下票数就好
> 2. Redis中有记录而Mysql缺记录: 主要发生在==Kafka丢消息、消息延迟==、出现异常导致创建订单等场景 
>    - ==删除Redis余票和座位缓存== 重新从Mysql同步
>    - 向mysql补数据很麻烦，因为有可能在马上要执行这个补的操作时候，Redis发生主节点宕机，从节点切换后这个数据丢失了，所以不要以一个不稳定的数据源作为依据，除非能容忍丢失 
>    - 消息延迟的记录可以直接通过数据回滚进行恢复
>    - Redis中扣减的那张票直接废弃就好 反正Mysql订单没创建成功 票也没少 用户在前端页面也不会跳到支付页面支付
> 3. Mysql有记录而Redis缺记录:发生在==Redis的AOF/RDB未刷盘宕机、主从切换未同步导致数据丢失==
>    - 直接==补记录然后删除缓存==重新同步即可

==清除的是Redis下有异常的票档的座位和余票信息==

1. 在Redis扣减库存成功后，Redis宕机了怎么办？

   > 主从+哨兵 但是可能会发生主从切换导致Redis中余票和座位未扣减但是Mysql已经下单 通过记录比对删除Redis缓存同步Mysql可以解决

2. 通过lua在Redis执行扣减库存执行过程中，Redis宕机了怎么办？

   > 这时候会抛出异常,删除缓存重新在Mysql同步即可

3. 扣减库存成功后，Kakfa宕机了怎么办？

   > - Kafka集群+同步刷盘可以解决
   >
   > - 会有Redis操作记录而Mysql没有 对账时可以发现并重构缓存 

4. 扣减库存成功后，Kakfa的消息丢了，没有创建出订单怎么办？

   > 同3

5. 扣减库存成功后，Kafka没有宕机，但收到消息延迟了怎么办？

   > 订单丢弃并回滚

这样看起来补记录意义不太大?毕竟数据都同步完成了 是用来给管理员看的吗

==总结起来就是对账记录的时候不一致则删除Redis中余票和座位信息缓存,以Mysql的数据为准重构缓存==

------

#  	 用户注册业务

![img](assets/1723689547717-cf815238-633e-4fc9-afc5-d33b530aebbd.png)

**关键组件：**

1. lua脚本
2. 验证码组件
3. 校验树
4. 布隆过滤器

## 校验操作

```
检查是否需要验证码：无参数
获取验证码：
captchaType: 验证码类型（如blockPuzzle滑块、clickWord点选）
验证验证码：
captchaType: 验证码类型
pointJson: 用户操作坐标或距离（可能经过AES加密）
token: 验证码唯一标识
注册时二次验证：
captchaId: 验证码ID
captchaVerification: 验证码验证结果（用于后端二次校验）
```

1. 用户发起注册请求
   -> 业务服务生成 captchaId
   -> 返回 captchaId 给前端
2. 前端请求获取验证码 
   -> 验证码服务生成验证码和 token
   -> 返回验证码图片和 token 给前端

所需参数：

- RemoteAddr(浏览器自带)

  ```java
   RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
          assert requestAttributes != null;
          HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
          captchaVO.setBrowserInfo(RemoteUtil.getRemoteId(request));
  ```

- ClientUid(使用 Md5Util.md5() 方法对 browserInfo 进行 MD5 加密，作为 ClientUid或者通过前端手动传入的ClientUid) 作验证码重试次数限流

  ```java
  protected String getValidateClientId(CaptchaVO req){
      	// 以服务端获取的客户端标识 做识别标志
  		if(StringUtils.isNotEmpty(req.getBrowserInfo())){
  			return Md5Util.md5(req.getBrowserInfo());
  		}
  		// 以客户端Ui组件id做识别标志
  		if(StringUtils.isNotEmpty(req.getClientUid())){
  			return req.getClientUid();
  		}
      	return null;
  	}
  ```

- type(前端传入验证码类型)

  ```java
  public ResponseModel get(CaptchaVO captchaVO) {
          if (captchaVO == null) {
              return RepCodeEnum.NULL_ERROR.parseError("captchaVO");
          }
          if (StringUtils.isEmpty(captchaVO.getCaptchaType())) {
              return RepCodeEnum.NULL_ERROR.parseError("类型");
          }
          return getService(captchaVO.getCaptchaType()).get(captchaVO);
      }
  ```

3. 用户完成验证码验证 
   -> 前端提交 token 和验证结果给业务服务

后端将token+坐标信息、value加密组合作为key token作为value存入redis用于二次校验

> 将 token 和坐标信息用 AES 加密生成 value
> 以 REDIS_SECOND_CAPTCHA_KEY 为前缀，将加密后的 value 作为键，token 作为值存储到缓存中
> 设置过期时间为 3 分钟 (EXPIRE_SIN_THREE)
> 将加密后的 value 设置为 captchaVerification 返回给前端

返回CaptchaVerification

4. 用户提交注册请求
   -> 前端提交 captchaId 和CaptchaVerification
   -> 业务服务通过 captchaId 关联并验证验证码

```java
@Override
public ResponseModel verification(CaptchaVO captchaVO) {
    if (captchaVO == null) {
        return RepCodeEnum.NULL_ERROR.parseError("captchaVO");
    }
    if (StringUtils.isEmpty(captchaVO.getCaptchaVerification())) {
        return RepCodeEnum.NULL_ERROR.parseError("二次校验参数");
    }
    try {
        String codeKey = String.format(REDIS_SECOND_CAPTCHA_KEY, captchaVO.getCaptchaVerification());
        if (!CaptchaServiceFactory.getCache(cacheType).exists(codeKey)) {
            return ResponseModel.errorMsg(RepCodeEnum.API_CAPTCHA_INVALID);
        }
        //二次校验取值后，即刻失效
        CaptchaServiceFactory.getCache(cacheType).delete(codeKey);
    } catch (Exception e) {
        logger.error("验证码坐标解析失败", e);
        return ResponseModel.errorMsg(e.getMessage());
    }
    return ResponseModel.success();
}
```

以传入的 captchaVerification 作为键，构造缓存键 codeKey

- 检查该键在缓存中是否存在

- 如果存在，则删除该键（一次性使用，即刻失效）

- 返回验证成功结果

![image-20250917200914185](assets/image-20250917200914185.png)
