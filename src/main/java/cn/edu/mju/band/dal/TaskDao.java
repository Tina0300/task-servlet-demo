package cn.edu.mju.band.dal;

import cn.edu.mju.band.entity.Task;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 环节三：继承 BaseMapper 即获得 insert / deleteById / updateById / selectById /
 * selectList / selectPage 等 17 个通用方法，原先手写的 6 个方法与 TaskMapper.xml 全部删除。
 * 接口不加 @Mapper 也可以——启动类上的 @MapperScan 统一注册。
 */
public interface TaskDao extends BaseMapper<Task> {
}
