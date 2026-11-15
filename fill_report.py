# -*- coding: utf-8 -*-
"""
Fill the Java advanced programming experiment report (docx).
Input template:
  D:\\迅雷下载\\04_完整学号_姓名_题目_设计报告(单元测试与 SpringBoot 快速迁移) (1).docx
Output:
  D:\\迅雷下载\\04_3242508223_邹甜_单元测试与SpringBoot快速迁移_设计报告.docx
"""
from docx import Document
from docx.shared import Pt, RGBColor
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
from docx.text.paragraph import Paragraph

SRC = r"D:\迅雷下载\04_完整学号_姓名_题目_设计报告(单元测试与 SpringBoot 快速迁移) (1).docx"
OUT = r"D:\迅雷下载\04_3242508223_邹甜_单元测试与SpringBoot快速迁移_设计报告.docx"

doc = Document(SRC)

# ---------------- helpers ----------------

def set_east_asia(run, font_name):
    run.font.name = font_name
    rPr = run._element.get_or_add_rPr()
    rFonts = rPr.find(qn("w:rFonts"))
    if rFonts is None:
        rFonts = OxmlElement("w:rFonts")
        rPr.append(rFonts)
    rFonts.set(qn("w:eastAsia"), font_name)


class Cursor:
    """Insert blocks right after an anchor paragraph, keeping order."""

    def __init__(self, anchor):
        self.el = anchor._p
        self.parent = anchor._parent

    def _new_paragraph(self):
        new_p = OxmlElement("w:p")
        self.el.addnext(new_p)
        self.el = new_p
        return Paragraph(new_p, self.parent)

    def heading(self, text):
        p = self._new_paragraph()
        run = p.add_run(text)
        run.bold = True
        run.font.size = Pt(12)
        set_east_asia(run, "黑体")
        p.paragraph_format.space_before = Pt(6)
        p.paragraph_format.space_after = Pt(3)
        return p

    def body(self, text):
        p = self._new_paragraph()
        run = p.add_run(text)
        run.font.size = Pt(11)
        set_east_asia(run, "宋体")
        p.paragraph_format.first_line_indent = Pt(22)
        p.paragraph_format.space_after = Pt(2)
        p.paragraph_format.line_spacing = 1.25
        return p

    def bullet(self, text):
        p = self._new_paragraph()
        run = p.add_run("• " + text)
        run.font.size = Pt(11)
        set_east_asia(run, "宋体")
        p.paragraph_format.left_indent = Pt(18)
        p.paragraph_format.space_after = Pt(1)
        return p

    def code(self, code):
        p = self._new_paragraph()
        lines = code.split("\n")
        for i, line in enumerate(lines):
            run = p.add_run(line)
            run.font.name = "Consolas"
            run.font.size = Pt(9)
            run.font.color.rgb = RGBColor(0x1F, 0x1F, 0x1F)
            if i < len(lines) - 1:
                run.add_break()
        pf = p.paragraph_format
        pf.left_indent = Pt(12)
        pf.space_before = Pt(2)
        pf.space_after = Pt(4)
        # light gray shading
        pPr = p._p.get_or_add_pPr()
        shd = OxmlElement("w:shd")
        shd.set(qn("w:val"), "clear")
        shd.set(qn("w:fill"), "F2F2F2")
        pPr.append(shd)
        return p

    def table(self, headers, rows):
        tbl = doc.add_table(rows=1, cols=len(headers))
        try:
            tbl.style = "Table Grid"
        except Exception:
            pass
        hdr = tbl.rows[0].cells
        for i, h in enumerate(headers):
            hdr[i].text = ""
            r = hdr[i].paragraphs[0].add_run(h)
            r.bold = True
            r.font.size = Pt(10)
            set_east_asia(r, "宋体")
        for row in rows:
            cells = tbl.add_row().cells
            for i, v in enumerate(row):
                cells[i].text = ""
                r = cells[i].paragraphs[0].add_run(str(v))
                r.font.size = Pt(10)
                set_east_asia(r, "宋体")
        self.el.addnext(tbl._tbl)
        self.el = tbl._tbl
        return tbl


def find_heading(prefix):
    for p in doc.paragraphs:
        t = p.text.strip()
        if t.startswith(prefix):
            return p
    raise RuntimeError("heading not found: " + prefix)

# ---------------- cover table ----------------
cover = doc.tables[0]
cover_values = {
    "姓名": "邹甜",
    "学号": "3242508223",
    "完成日期": "2026 年 11 月 2 日",
}
for row in cover.rows:
    label = row.cells[0].text.strip()
    if label in cover_values:
        cell = row.cells[1]
        cell.text = ""
        run = cell.paragraphs[0].add_run(cover_values[label])
        run.font.size = Pt(11)
        set_east_asia(run, "宋体")

# ---------------- 三、分析问题和确定解决方案 ----------------
c = Cursor(find_heading("三、"))
c.body("本项目原为基于 Servlet/JSP 的传统 SSM 工程，采用 war 包部署。迁移前存在配置样板多、"
       "环境切换困难、依赖外部容器、无自动化测试等问题。按分层架构，各关键层的调整原因如下。")

c.heading("1. 配置层：删除 web.xml、applicationContext.xml、spring-mvc.xml")
c.body("传统工程通过 web.xml 手工注册 DispatcherServlet、过滤器与监听器；通过 applicationContext.xml "
       "手工声明数据源、SqlSessionFactory、Mapper 扫描器。这些配置存在大量样板代码，且与环境强绑定。"
       "Spring Boot 基于“约定优于配置”，由 Starter 依赖触发自动配置类（AutoConfiguration），"
       "通过条件注解按需注册 Bean，因此旧式 XML 可以全部移除。")

c.heading("2. 视图层：JSP 迁移为 Thymeleaf")
c.body("可执行 jar 模式不支持 JSP：JSP 依赖 war 目录结构，运行时由 Jasper 编译器编译，"
       "而 jar 内部没有可写的资源目录。Thymeleaf 模板是纯 HTML 文件，随 jar 一起打包，"
       "由 ClassLoader 直接加载，天然适配 jar 部署，因此 login.jsp 等页面改写为 templates 下的 HTML。")

c.heading("3. 容器层：外部 Tomcat 替换为内嵌 Tomcat")
c.body("外部 Tomcat 需要独立安装、手工部署 war，开发与生产环境容易不一致。spring-boot-starter-web "
       "已以内嵌方式集成 Tomcat，主启动类的 main 方法即可在同一 JVM 内启动 Web 容器，"
       "部署物由“war + 外部容器”简化为“一个可执行 jar + JDK”。")

c.heading("4. 数据层：手工装配替换为 DataSource 自动配置与 MyBatis Starter")
c.body("mybatis-spring-boot-starter 自动创建 SqlSessionFactory 并完成 Mapper 接口扫描；"
       "数据源由 application 配置驱动，Druid 连接池参数集中管理，"
       "消除了 XML 中冗长的 Bean 声明，配置变更只需修改 yml 文件。")

c.heading("5. 配置管理层：单文件改造为多环境 Profile 架构")
c.body("开发、测试、生产环境的数据源、端口、日志级别各不相同。Profile 机制将公共配置与环境配置分离，"
       "通过命令行参数即可切换，避免手工改配置引发的错误，也便于持续集成流水线按环境发布。")

c.heading("6. 测试层：引入 JUnit 5 与 Mockito")
c.body("业务逻辑需要可重复、可自动化的验证手段。Mockito 可以对持久层打桩，"
       "使测试不依赖物理数据库与外部网络，数据库宕机、断网等极端场景也能稳定复现和断言。")

# ---------------- 四、详细设计 ----------------
c = Cursor(find_heading("四、"))

c.heading("1. 目标项目结构")
c.code(
    "task-servlet-demo/\n"
    "├─ src/main/java/cn/edu/mju/band/\n"
    "│   ├─ TaskApplication.java        # 主启动类\n"
    "│   ├─ controller/                 # Index/Login/Task 控制器\n"
    "│   ├─ filter/AuthFilter.java      # 登录鉴权过滤器\n"
    "│   ├─ buss/ + buss/impl/          # 业务接口与实现\n"
    "│   ├─ dal/                        # TaskDao、UserDao 接口\n"
    "│   └─ entity/                     # Task、User 实体\n"
    "└─ src/main/resources/\n"
    "    ├─ application.yml             # 公共配置，默认 dev\n"
    "    ├─ application-dev/test/prod.yml\n"
    "    ├─ mapper/*.xml                # MyBatis SQL 映射\n"
    "    ├─ templates/*.html            # Thymeleaf 页面\n"
    "    └─ static/css/style.css"
)

c.heading("2. pom.xml 设计")
c.bullet("继承 spring-boot-starter-parent 3.4.13，打包方式由 war 改为 jar；")
c.bullet("引入 spring-boot-starter-web（含内嵌 Tomcat）、thymeleaf、jdbc；")
c.bullet("引入 mybatis-spring-boot-starter 3.0.4、mysql-connector-j、druid-spring-boot-3-starter 1.2.23、hutool；")
c.bullet("配置 spring-boot-maven-plugin（repackage 生成可执行 jar）、m2e 与 JaCoCo 相关插件。")

c.heading("3. 主启动类设计")
c.body("使用 @SpringBootApplication 开启组件扫描与自动配置，使用 @MapperScan 统一扫描 DAO 接口，"
       "main 方法调用 SpringApplication.run 完成容器启动。")

c.heading("4. 多环境配置设计")
c.table(
    ["环境", "端口", "数据源", "特点"],
    [
        ["dev", "8080", "task_db（本地 MySQL）", "模板缓存关闭，debug 日志"],
        ["test", "8081", "task_db_test", "支持环境变量覆盖账号密码"],
        ["prod", "8080", "环境变量 DB_URL 等注入", "模板缓存开启，warn 日志"],
    ],
)
c.body("主配置文件声明 spring.profiles.active=dev 作为默认环境；"
       "命令行追加 --spring.profiles.active=test 即可覆盖，启动日志会打印当前激活的 Profile。")

c.heading("5. 分层组件设计")
c.bullet("Controller 层：@Controller 处理路由与表单绑定，通过构造器注入业务层；")
c.bullet("Service 层：接口 + 实现类，@Service 注册 Bean，@Transactional 声明事务，"
         "add 方法包含标题合法性校验与默认值设置；")
c.bullet("DAO 层：MyBatis 接口与 mapper XML 的 namespace 一一对应；")
c.bullet("过滤器：@Component 注册 AuthFilter，白名单前缀匹配，未登录请求重定向到 /login。")

c.heading("6. 测试设计")
c.body("共设计 4 个测试类、34 个用例，覆盖正常流、异常流与边界条件；"
       "每个用例通过 @BeforeEach 重建测试数据，Mockito 为每个方法提供全新 Mock，保证原子性与独立性。")

# ---------------- 五、实现 ----------------
c = Cursor(find_heading("五、"))

c.heading("1. 主启动类实现")
c.code(
    "@SpringBootApplication\n"
    "@MapperScan(\"cn.edu.mju.band.dal\")\n"
    "public class TaskApplication {\n"
    "    public static void main(String[] args) {\n"
    "        SpringApplication.run(TaskApplication.class, args);\n"
    "    }\n"
    "}"
)

c.heading("2. 多环境与 MyBatis 自动配置（application.yml 节选）")
c.code(
    "spring:\n"
    "  profiles:\n"
    "    active: dev\n"
    "mybatis:\n"
    "  mapper-locations: classpath*:mapper/*.xml\n"
    "  type-aliases-package: cn.edu.mju.band.entity\n"
    "  configuration:\n"
    "    map-underscore-to-camel-case: true\n"
    "    call-setters-on-nulls: true"
)

c.heading("3. 业务层合法性校验与默认值实现")
c.code(
    "@Override\n"
    "@Transactional(rollbackFor = Exception.class)\n"
    "public void add(Task task) {\n"
    "    if (task == null || task.getTitle() == null\n"
    "            || task.getTitle().isBlank()) {\n"
    "        throw new IllegalArgumentException(\"任务标题不能为空\");\n"
    "    }\n"
    "    if (task.getStatus() == null)   task.setStatus(0);\n"
    "    if (task.getPriority() == null) task.setPriority(2);\n"
    "    taskDao.insert(task);\n"
    "}"
)

c.heading("4. Mockito 打桩实现（数据库宕机场景节选）")
c.code(
    "@Test\n"
    "void add_whenDatabaseDown_shouldThrowAndStillApplyDefaults() {\n"
    "    doThrow(new DataAccessResourceFailureException(\"Communications link failure\"))\n"
    "            .when(taskDao).insert(any(Task.class));\n"
    "    assertThrows(DataAccessResourceFailureException.class,\n"
    "            () -> taskBuss.add(task));\n"
    "    assertEquals(0, task.getStatus());\n"
    "    verify(taskDao).insert(any(Task.class));\n"
    "}"
)

c.heading("5. 环境切换命令")
c.code("java -jar target/task-servlet-demo.jar --spring.profiles.active=test")

c.heading("6. 运行结果")
c.bullet("mvn clean verify 构建成功，输出 BUILD SUCCESS，生成 target/task-servlet-demo.jar；")
c.bullet("启动日志显示 The following 1 profile is active: \"dev\"，"
         "Tomcat started on port 8080 (http) with context path '/'；")
c.bullet("浏览器访问 http://localhost:8080/login 显示登录页面，使用 admin/123456 可正常登录并进入任务列表。")

# ---------------- 六、测试 ----------------
c = Cursor(find_heading("六、"))

c.heading("（1）测试结果")
c.table(
    ["测试类", "用例数", "覆盖内容", "结果"],
    [
        ["TaskBussImplTest", "12", "增删改查、默认值、空标题校验、委托与异常", "全部通过"],
        ["UserBussImplTest", "8", "登录成功/失败、空用户名、密码错误、trim 边界", "全部通过"],
        ["TaskTest", "3", "isFinished 三种状态分支", "全部通过"],
        ["ServiceResilienceTest", "11", "数据库宕机、断网、连续打桩、校验先行", "全部通过"],
        ["合计", "34", "Failures: 0, Errors: 0, Skipped: 0", "BUILD SUCCESS"],
    ],
)
c.body("JaCoCo 覆盖率报告（target/site/jacoco/index.html）显示：核心业务层 buss.impl 行覆盖率 100%、"
       "分支覆盖率 90%；entity 包指令覆盖率 81%；全项目指令覆盖率 43%、分支覆盖率 60%，"
       "未覆盖部分主要是本次未编写测试的 controller 与 filter 包。")

c.heading("（2）调试过程中遇到的问题及解决方法")
c.bullet("问题一：IDEA 仍使用 Smart Tomcat 配置，docBase 指向已删除的 webapp，浏览器出现 Tomcat 原生 404。"
         "解决：改为运行 TaskApplication 的 main 方法，由内嵌 Tomcat 提供服务。")
c.bullet("问题二：MySQL 服务显示 Running，但旧客户端报 ERROR 2003（10061）。"
         "解决：改用 MySQL 8.0 官方客户端并显式指定协议、主机与端口。")
c.bullet("问题三：报 ERROR 2026 SSL connection error。"
         "解决：本地连接添加 --ssl-mode=DISABLED 禁用 SSL。")
c.bullet("问题四：报 ERROR 2061 requires secure connection。"
         "解决：添加 --get-server-public-key 获取服务端公钥完成密码交换。")
c.bullet("问题五：应用报 1045 Access denied。"
         "解决：将数据库密码与 db 配置统一为 Root@123456。")
c.bullet("问题六：插入中文报 ERROR 1406 Data too long。"
         "解决：执行 SET NAMES utf8mb4 修正连接字符集，并在测试脚本中预置该语句。")
c.bullet("问题七：JDK 26 下 Mockito 自挂载代理产生警告。"
         "解决：通过 maven-dependency-plugin 取得 Mockito 路径，在 Surefire 中显式配置 -javaagent。")

# ---------------- 七、结果分析 ----------------
c = Cursor(find_heading("七、"))

c.heading("（1）调整后的优势")
c.bullet("配置精简：Spring 相关 XML 全部移除，自动配置按需生效，新增功能只需引入对应 Starter；")
c.bullet("部署简单：一个可执行 jar 加 JDK 即可运行，不再依赖外部 Tomcat 安装与 war 部署；")
c.bullet("环境隔离：dev/test/prod 三套配置结构清晰，命令行参数一键切换，适合持续集成；")
c.bullet("测试可靠：纯 Mockito 单元测试离线可运行，数据库宕机、断网场景可稳定复现，结果可重复；")
c.bullet("改进可度量：JaCoCo 报告精确到行，核心业务层行覆盖率达到 100%，重构有数据支撑。")

c.heading("（2）经验和体会")
c.bullet("“约定优于配置”不是没有配置，而是把通用配置沉淀为自动配置，开发者只保留差异化部分；")
c.bullet("排查问题要先定位日志最底部的 Caused by，按根因选择通道，而不是盲目重试；")
c.bullet("单元测试是重构的安全网：先补校验逻辑、再补测试、最后看覆盖率，改动才敢落地；")
c.bullet("安全意识同样重要：生产环境密码必须通过环境变量注入，明文密码存储应升级为加盐哈希；")
c.bullet("版本升级要关注兼容性细节，如 Boot 3 的 jakarta 包名、Druid 的 Boot 3 专用坐标、JDK 新特性对代理加载的限制。")

# ---------------- 八、参考文献 ----------------
c = Cursor(find_heading("八、"))
refs = [
    "Spring Boot Reference Documentation. https://docs.spring.io/spring-boot/",
    "MyBatis Spring Boot Starter. https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/",
    "JUnit 5 User Guide. https://junit.org/junit5/docs/current/user-guide/",
    "Mockito Framework Documentation. https://site.mockito.org/",
    "JaCoCo Java Code Coverage Library. https://www.jacoco.org/jacoco/trunk/doc/",
    "Thymeleaf + Spring Documentation. https://www.thymeleaf.org/documentation.html",
]
for i, r in enumerate(refs, 1):
    c.bullet(f"[{i}] {r}")

# ---------------- 九、注意事项 ----------------
c = Cursor(find_heading("九、"))
notes = [
    "Spring Boot 3 的 Servlet API 包名为 jakarta.*，不再使用 javax.*，导入过滤器时注意区分；",
    "Boot 3 集成 Druid 必须使用 druid-spring-boot-3-starter 坐标，旧坐标不会自动装配；",
    "可执行 jar 模式不支持 JSP，视图必须使用 Thymeleaf 等模板引擎并放在 templates 目录；",
    "修改配置后需重新执行 Maven 构建，确认 target/classes 与 jar 内的资源同步更新；",
    "生产环境的数据源账号密码必须通过环境变量注入，禁止硬编码在配置文件中；",
    "MockitoExtension 默认严格模式，未被使用的打桩会报错，不应随意使用 lenient 绕过；",
    "JDK 26 等新版本下需显式挂载 Mockito 代理，避免动态代理加载在未来版本被禁用；",
    "含中文的 SQL 脚本执行前先确认连接字符集为 utf8mb4，避免出现数据过长或乱码问题。",
]
for n in notes:
    c.bullet(n)

doc.save(OUT)
print("saved:", OUT)
print("paragraphs:", len(doc.paragraphs), "tables:", len(doc.tables))
