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
    /// App.xaml 的交互逻辑
    /// </summary>
    public partial class App : Application
    {
        private IHost? _host;
        private ILogger<App>? _logger;
        
        /// <summary>
        /// 应用程序启动事件
        /// </summary>
        protected override async void OnStartup(StartupEventArgs e)
        {
            try
            {
                // 注册全局异常处理
                AppDomain.CurrentDomain.UnhandledException += OnUnhandledException;
                DispatcherUnhandledException += OnDispatcherUnhandledException;
                // 创建主机构建器
                var hostBuilder = Host.CreateDefaultBuilder(e.Args)
                    .ConfigureAppConfiguration((context, config) =>
                    {
                        // 配置文件路径
                        var basePath = AppDomain.CurrentDomain.BaseDirectory;
                        config.SetBasePath(basePath);
                        config.AddJsonFile("appsettings.json", optional: false, reloadOnChange: true);
                    })
                    .ConfigureServices((context, services) =>
                    {
                        ConfigureServices(services, context.Configuration);
                    })
                    .ConfigureLogging((context, logging) =>
                    {
                        ConfigureLogging(logging, context.Configuration);
                    });
                
                // 构建主机
                _host = hostBuilder.Build();
                
                // 启动主机
                await _host.StartAsync();
                
                // 获取日志记录器
                _logger = _host.Services.GetRequiredService<ILogger<App>>();
                _logger.LogInformation("应用程序启动成功");
                
                // 初始化数据库
                await InitializeDatabaseAsync();
                
                // 创建并显示主窗口
                var mainWindow = new MainWindow();
                var dataCollectionService = _host.Services.GetRequiredService<DataCollectionService>();
                var mainWindowLogger = _host.Services.GetRequiredService<ILogger<MainWindow>>();
                
                // 初始化MainWindow的服务
                mainWindow.InitializeServices(dataCollectionService, mainWindowLogger);
                
                // 确保窗口正常显示
                mainWindow.WindowState = WindowState.Normal;
                mainWindow.Show();
                mainWindow.Activate();
                mainWindow.Focus();
                
                base.OnStartup(e);
            }
            catch (Exception ex)
            {
                var errorMessage = $"应用程序启动失败: {ex.Message}";
                MessageBox.Show(errorMessage, "启动错误", MessageBoxButton.OK, MessageBoxImage.Error);
                
                _logger?.LogCritical(ex, "应用程序启动失败");
                Shutdown(1);
            }
        }
        
        /// <summary>
        /// 配置服务
        /// </summary>
        private void ConfigureServices(IServiceCollection services, IConfiguration configuration)
        {
            try
            {
                // 注册配置
                services.AddSingleton(configuration);
                
                // 注册数据库服务
                var connectionString = configuration.GetConnectionString("DefaultConnection") 
                    ?? configuration["DatabaseSettings:ConnectionString"] 
                    ?? "Data Source=plc_data.db;Cache=Shared";
                
                services.AddSingleton<DatabaseService>(provider =>
                {
                    var logger = provider.GetRequiredService<ILogger<DatabaseService>>();
                    return new DatabaseService(connectionString, logger);
                });
                
                // 注册PLC服务
                services.AddSingleton<PLCService>();
                
                // 注册数据采集服务
                services.AddSingleton<DataCollectionService>();
                
                // 注册主窗口相关的日志记录器
                services.AddSingleton<ILogger<MainWindow>>(provider => 
                    provider.GetRequiredService<ILoggerFactory>().CreateLogger<MainWindow>());
                
                // 注册后台服务
                services.AddHostedService<DataCollectionService>(provider => 
                    provider.GetRequiredService<DataCollectionService>());
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("配置服务失败", ex);
            }
        }
        
        /// <summary>
        /// 配置日志
        /// </summary>
        private void ConfigureLogging(ILoggingBuilder logging, IConfiguration configuration)
        {
            try
            {
                // 清除默认日志提供程序
                logging.ClearProviders();
                
                // 添加控制台日志
                logging.AddConsole();
                
                // 添加调试日志
                logging.AddDebug();
                
                // 从配置文件读取日志级别
                var logLevel = configuration["LoggingSettings:LogLevel"];
                if (Enum.TryParse<LogLevel>(logLevel, out var level))
                {
                    logging.SetMinimumLevel(level);
                }
                else
                {
                    logging.SetMinimumLevel(LogLevel.Information);
                }
                
                // 配置文件日志（如果启用）
                var logToFile = configuration.GetValue<bool>("LoggingSettings:LogToFile");
                if (logToFile)
                {
                    ConfigureFileLogging(logging, configuration);
                }
            }
            catch (Exception ex)
            {
                throw new InvalidOperationException("配置日志失败", ex);
            }
        }
        
        /// <summary>
        /// 配置文件日志
        /// </summary>
        private void ConfigureFileLogging(ILoggingBuilder logging, IConfiguration configuration)
        {
            try
            {
                var logFilePath = configuration["LoggingSettings:LogFilePath"] ?? "logs/plc_collector_{Date}.log";
                var logsDirectory = Path.GetDirectoryName(logFilePath);
                
                if (!string.IsNullOrEmpty(logsDirectory) && !Directory.Exists(logsDirectory))
                {
                    Directory.CreateDirectory(logsDirectory);
                }
                
                // 这里可以添加第三方文件日志提供程序，如Serilog
                // 由于简化实现，暂时使用控制台日志
            }
            catch (Exception ex)
            {
                // 文件日志配置失败不应该阻止应用程序启动
                Console.WriteLine($"配置文件日志失败: {ex.Message}");
            }
        }
        
        /// <summary>
        /// 初始化数据库
        /// </summary>
        private async Task InitializeDatabaseAsync()
        {
            try
            {
                var databaseService = _host?.Services.GetRequiredService<DatabaseService>();
                if (databaseService != null)
                {
                    await databaseService.InitializeDatabaseAsync();
                    _logger?.LogInformation("数据库初始化完成");
                }
            }
            catch (Exception ex)
            {
                _logger?.LogError(ex, "数据库初始化失败");
                throw new InvalidOperationException("数据库初始化失败", ex);
            }
        }
        
        /// <summary>
        /// 应用程序退出事件
        /// </summary>
        protected override async void OnExit(ExitEventArgs e)
        {
            try
            {
                _logger?.LogInformation("应用程序正在退出...");
                
                if (_host != null)
                {
                    // 停止主机
                    await _host.StopAsync(TimeSpan.FromSeconds(5));
                    _host.Dispose();
                }
                
                _logger?.LogInformation("应用程序已退出");
            }
            catch (Exception ex)
            {
                _logger?.LogError(ex, "应用程序退出时发生异常");
            }
            finally
            {
                base.OnExit(e);
            }
        }
        

        
        /// <summary>
        /// 处理未捕获的异常
        /// </summary>
        private void OnUnhandledException(object sender, UnhandledExceptionEventArgs e)
        {
            try
            {
                var exception = e.ExceptionObject as Exception;
                _logger?.LogCritical(exception, "发生未处理的异常");
                
                var message = exception?.Message ?? "发生未知错误";
                MessageBox.Show($"程序发生严重错误：{message}\n\n程序将退出。", "严重错误", 
                    MessageBoxButton.OK, MessageBoxImage.Error);
            }
            catch
            {
                // 异常处理中的异常，避免无限循环
            }
        }
        
        /// <summary>
        /// 处理UI线程未捕获的异常
        /// </summary>
        private void OnDispatcherUnhandledException(object sender, System.Windows.Threading.DispatcherUnhandledExceptionEventArgs e)
        {
            try
            {
                _logger?.LogError(e.Exception, "UI线程发生未处理的异常");
                
                var result = MessageBox.Show(
                    $"程序发生错误：{e.Exception.Message}\n\n是否继续运行程序？", 
                    "程序错误", 
                    MessageBoxButton.YesNo, 
                    MessageBoxImage.Warning);
                
                if (result == MessageBoxResult.Yes)
                {
                    e.Handled = true; // 标记异常已处理，继续运行
                }
                else
                {
                    Shutdown(1); // 退出程序
                }
            }
            catch
            {
                // 异常处理中的异常，避免无限循环
                e.Handled = false;
            }
        }
    }
}