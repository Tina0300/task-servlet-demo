package cn.edu.mju.band.buss;

import cn.edu.mju.band.buss.impl.UserBussImpl;
import cn.edu.mju.band.dal.UserDao;
import cn.edu.mju.band.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 环节三改造后：用户名查询由 selectOne + 条件构造器完成，
 * 打桩目标由 selectByUsername 改为 selectOne。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserBussImpl 业务层测试")
class UserBussImplTest {

    /**
     * 纯单元测试没有 Spring 上下文，MyBatis-Plus 的 TableInfo（实体属性↔列名映射）
     * 不会被自动装配。渲染 LambdaQueryWrapper 时要靠它把 User::getUsername 解析成 username，
     * 缺了就会抛 "can not find lambda cache for this entity"。这里按 MP 初始化方式补一次。
     */
    @BeforeAll
    static void initMybatisPlusTableInfo() {
        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, User.class);
    }

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserBussImpl userBuss;

    private User storedUser;

    @BeforeEach
    void setUp() {
        storedUser = new User();
        storedUser.setId(1L);
        storedUser.setUsername("admin");
        storedUser.setPassword("123456");
        storedUser.setRealName("admin");
    }

    // ---------------- 正常流 ----------------

    @Test
    @DisplayName("正常流：账号密码正确，返回用户对象")
    void login_shouldReturnUser_whenCredentialsCorrect() {
        when(userDao.selectOne(any())).thenReturn(storedUser);

        User result = userBuss.login("admin", "123456");

        assertSame(storedUser, result);
    }

    // ---------------- 异常流 ----------------

    @Test
    @DisplayName("异常流：用户名为 null，直接返回 null 且不访问 DAO")
    void login_shouldReturnNull_whenUsernameNull() {
        User result = userBuss.login(null, "123456");

        assertNull(result);
        verifyNoInteractions(userDao);
    }

    @Test
    @DisplayName("异常流：用户名为空白串，返回 null 且不访问 DAO")
    void login_shouldReturnNull_whenUsernameBlank() {
        User result = userBuss.login("   ", "123456");

        assertNull(result);
        verifyNoInteractions(userDao);
    }

    @Test
    @DisplayName("异常流：用户不存在，返回 null")
    void login_shouldReturnNull_whenUserNotFound() {
        // 条件构造器查不到记录时 selectOne 返回 null
        assertNull(userBuss.login("ghost", "123456"));
        verify(userDao).selectOne(any());
    }

    @Test
    @DisplayName("异常流：密码错误，返回 null")
    void login_shouldReturnNull_whenPasswordWrong() {
        when(userDao.selectOne(any())).thenReturn(storedUser);

        assertNull(userBuss.login("admin", "wrong"));
    }

    @Test
    @DisplayName("异常流：密码为 null，返回 null")
    void login_shouldReturnNull_whenPasswordNull() {
        when(userDao.selectOne(any())).thenReturn(storedUser);

        assertNull(userBuss.login("admin", null));
    }

    // ---------------- 边界条件 ----------------

    @Test
    @DisplayName("边界：用户名带前后空格，trim 后作为查询条件传给持久层")
    void login_shouldTrimUsername() {
        when(userDao.selectOne(any())).thenReturn(storedUser);

        User result = userBuss.login("  admin  ", "123456");

        assertSame(storedUser, result);

        @SuppressWarnings("rawtypes")
        ArgumentCaptor<LambdaQueryWrapper> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(userDao).selectOne(captor.capture());
        LambdaQueryWrapper wrapper = captor.getValue();
        // 3.5.9 起条件片段是惰性求值：eq() 只登记片段，绑定值要等 SQL 片段被渲染时才写入参数表。
        // 不先触发一次 getSqlSegment()，paramNameValuePairs 里就是空的。
        wrapper.getSqlSegment();
        // 条件构造器的参数表里保存的是 trim 之后的用户名
        assertTrue(wrapper.getParamNameValuePairs().containsValue("admin"));
    }

    @Test
    @DisplayName("getById：委托给 BaseMapper 的 selectById")
    void getById_shouldDelegate() {
        when(userDao.selectById(1L)).thenReturn(storedUser);

        assertSame(storedUser, userBuss.getById(1L));
    }
}
