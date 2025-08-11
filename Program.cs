using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using S3PLCDataCollector.Services;
using System;
using System.IO;
using System.Threading.Tasks;
using System.Windows;

namespace S3PLCDataCollector
{
    /// <summary>
    /// 程序入口点
    /// </summary>
    public class Program
    {
        /// <summary>
        /// 应用程序主入口点
        /// </summary>
        [STAThread]
        public static async Task<int> Main(string[] args)
        {
            try
            {
                // 检查是否作为Windows服务运行
                var isService = WindowsServiceExtensions.IsWindowsService() || 
                               WindowsServiceExtensions.HasServiceArgument(args);
                
                if (isService)
                {
                    // 作为Windows服务运行
                    return await RunAsServiceAsync(args);
                }
                else
                {
                    // 作为GUI应用程序运行
                    return RunAsApplication(args);
                }
            }
            catch (Exception ex)
            {
                // 记录启动异常
                Console.WriteLine($"程序启动失败: {ex.Message}");
                Console.WriteLine($"详细信息: {ex}");
                return 1;
            }
        }
        
        /// <summary>
        /// 作为Windows服务运行
        /// </summary>
        private static async Task<int> RunAsServiceAsync(string[] args)
        {
            try
            {
                Console.WriteLine("正在启动Windows服务模式...");
                
                var host = CreateHostBuilder(args, true).Build();
                
                // 初始化数据库
                using (var scope = host.Services.CreateScope())
                {
                    var dbService = scope.ServiceProvider.GetRequiredService<DatabaseService>();
                    await dbService.InitializeDatabaseAsync();
                }
                
                // 运行服务
                WindowsServiceHost.RunService(host);
                
                return 0;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Windows服务运行失败: {ex.Message}");
                Console.WriteLine($"详细信息: {ex}");
                return 1;
            }
        }
        
        /// <summary>
        /// 作为GUI应用程序运行
        /// </summary>
        private static int RunAsApplication(string[] args)
        {
            try
            {
                Console.WriteLine("正在启动GUI应用程序模式...");
                
                var app = new App();
                app.InitializeComponent();
                return app.Run();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"应用程序启动失败:\n{ex.Message}\n\n详细信息:\n{ex}", 
                              "启动错误", MessageBoxButton.OK, MessageBoxImage.Error);
                return 1;
            }
        }
        
        /// <summary>
        /// 创建主机构建器
        /// </summary>
        public static IHostBuilder CreateHostBuilder(string[] args, bool isService = false)
        {
            var builder = Host.CreateDefaultBuilder(args)
                .ConfigureAppConfiguration((context, config) =>
                {
                    // 获取应用程序目录
                    var appDirectory = GetApplicationDirectory();
                    
                    // 配置文件路径
                    var configPath = Path.Combine(appDirectory, "appsettings.json");
                    
                    config.SetBasePath(appDirectory)
                          .AddJsonFile("appsettings.json", optional: false, reloadOnChange: true)
                          .AddJsonFile($"appsettings.{context.HostingEnvironment.EnvironmentName}.json", 
                                     optional: true, reloadOnChange: true)
                          .AddEnvironmentVariables()
                          .AddCommandLine(args);
                })
                .ConfigureLogging((context, logging) =>
                {
                    logging.ClearProviders();
                    
                    // 控制台日志（仅在非服务模式下）
                    if (!isService)
                    {
                        logging.AddConsole();
                    }
                    
                    // 文件日志
                    var logPath = context.Configuration.GetValue<string>("LoggingSettings:LogFilePath") ?? 
                                 Path.Combine(GetApplicationDirectory(), "logs", "app.log");
                    
                    // 处理日志文件路径中的日期占位符
                    if (logPath.Contains("{Date}"))
                    {
                        logPath = logPath.Replace("{Date}", DateTime.Now.ToString("yyyy-MM-dd"));
                    }
                    
                    // 确保日志目录存在
                    var logDirectory = Path.GetDirectoryName(logPath);
                    if (!Directory.Exists(logDirectory))
                    {
                        Directory.CreateDirectory(logDirectory);
                    }
                    
                    // 添加文件日志提供程序（这里需要第三方库如NLog或Serilog）
                    // 暂时使用控制台日志
                    logging.AddConsole();
                    logging.AddDebug();
                    
                    // 设置日志级别
                    var logLevel = context.Configuration.GetValue<string>("LoggingSettings:LogLevel");
                    if (Enum.TryParse<LogLevel>(logLevel, out var level))
                    {
                        logging.SetMinimumLevel(level);
                    }
                })
                .ConfigureServices((context, services) =>
                {
                    // 注册配置
                    services.Configure<PLCConfig>(context.Configuration.GetSection("PLCSettings"));
                    services.Configure<DatabaseConfig>(context.Configuration.GetSection("DatabaseSettings"));
                    services.Configure<ServiceConfig>(context.Configuration.GetSection("ServiceSettings"));
                    services.Configure<UIConfig>(context.Configuration.GetSection("UISettings"));
                    
                    // 注册服务
                    services.AddSingleton<DatabaseService>();
                    services.AddSingleton<PLCService>();
                    services.AddSingleton<DataCollectionService>();
                    services.AddSingleton<WindowsServiceInstaller>();
                    
                    // 注册后台服务
                    services.AddHostedService<DataCollectionService>();
                    
                    // 如果是服务模式，注册Windows服务生命周期
                    if (isService)
                    {
                        services.AddSingleton<IHostLifetime, WindowsServiceLifetime>();
                    }
                });
            
            return builder;
        }
        
        /// <summary>
        /// 获取应用程序目录
        /// </summary>
        private static string GetApplicationDirectory()
        {
            try
            {
                // 获取可执行文件所在目录
                var assembly = System.Reflection.Assembly.GetExecutingAssembly();
                var location = assembly.Location;
                
                if (string.IsNullOrEmpty(location))
                {
                    // 如果无法获取程序集位置，使用当前目录
                    return Directory.GetCurrentDirectory();
                }
                
                return Path.GetDirectoryName(location) ?? Directory.GetCurrentDirectory();
            }
            catch
            {
                // 如果发生异常，返回当前目录
                return Directory.GetCurrentDirectory();
            }
        }
    }
    
    /// <summary>
    /// PLC配置类
    /// </summary>
    public class PLCConfig
    {
        public string IPAddress { get; set; } = "192.168.1.2";
        public int Port { get; set; } = 502;
        public int ReadInterval { get; set; } = 5000;
        public int Timeout { get; set; } = 3000;
        public int RetryCount { get; set; } = 3;
    }
    
    /// <summary>
    /// 数据库配置类
    /// </summary>
    public class DatabaseConfig
    {
        public string ConnectionString { get; set; } = "Data Source=plc_data.db";
        public int LogRetentionDays { get; set; } = 30;
        public bool EnableBackup { get; set; } = true;
        public string BackupPath { get; set; } = "Backup";
    }
    
    /// <summary>
    /// Windows服务配置类
    /// </summary>
    public class ServiceConfig
    {
        public string ServiceName { get; set; } = "S3PLCDataCollectorService";
        public string DisplayName { get; set; } = "汇川S3 PLC数据采集服务";
        public string Description { get; set; } = "自动采集汇川S3 PLC数据并保存到数据库";
        public bool AutoStart { get; set; } = true;
    }
    
    /// <summary>
    /// UI配置类
    /// </summary>
    public class UIConfig
    {
        public int RefreshInterval { get; set; } = 1000;
        public int MaxDisplayRows { get; set; } = 1000;
        public bool AutoScrollLog { get; set; } = true;
        public bool ShowInTray { get; set; } = true;
    }
}