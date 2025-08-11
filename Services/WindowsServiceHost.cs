using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using System;
using System.ServiceProcess;
using System.Threading;
using System.Threading.Tasks;

namespace S3PLCDataCollector.Services
{
    /// <summary>
    /// Windows服务主机类
    /// </summary>
    public class WindowsServiceHost : ServiceBase
    {
        private readonly IHost _host;
        private readonly ILogger<WindowsServiceHost> _logger;
        private CancellationTokenSource _cancellationTokenSource;
        
        public WindowsServiceHost(IHost host)
        {
            _host = host;
            _logger = _host.Services.GetRequiredService<ILogger<WindowsServiceHost>>();
            _cancellationTokenSource = new CancellationTokenSource();
            
            // 设置服务属性
            ServiceName = "S3PLCDataCollectorService";
            CanStop = true;
            CanShutdown = true;
            CanPauseAndContinue = false;
            AutoLog = true;
        }
        
        /// <summary>
        /// 服务启动
        /// </summary>
        protected override async void OnStart(string[] args)
        {
            try
            {
                _logger.LogInformation("Windows服务正在启动...");
                
                // 重新创建取消令牌
                _cancellationTokenSource?.Dispose();
                _cancellationTokenSource = new CancellationTokenSource();
                
                // 启动主机
                await _host.StartAsync(_cancellationTokenSource.Token);
                
                _logger.LogInformation("Windows服务启动成功");
                
                base.OnStart(args);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Windows服务启动失败");
                ExitCode = 1;
                Stop();
            }
        }
        
        /// <summary>
        /// 服务停止
        /// </summary>
        protected override async void OnStop()
        {
            try
            {
                _logger.LogInformation("Windows服务正在停止...");
                
                // 取消所有操作
                _cancellationTokenSource?.Cancel();
                
                // 停止主机
                if (_host != null)
                {
                    await _host.StopAsync(TimeSpan.FromSeconds(30));
                }
                
                _logger.LogInformation("Windows服务停止成功");
                
                base.OnStop();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Windows服务停止时发生异常");
            }
        }
        
        /// <summary>
        /// 系统关闭时调用
        /// </summary>
        protected override void OnShutdown()
        {
            try
            {
                _logger.LogInformation("系统正在关闭，停止Windows服务...");
                OnStop();
                base.OnShutdown();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "系统关闭时停止服务发生异常");
            }
        }
        
        /// <summary>
        /// 释放资源
        /// </summary>
        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                try
                {
                    _cancellationTokenSource?.Cancel();
                    _cancellationTokenSource?.Dispose();
                    _host?.Dispose();
                }
                catch (Exception ex)
                {
                    _logger?.LogError(ex, "释放Windows服务资源时发生异常");
                }
            }
            
            base.Dispose(disposing);
        }
        
        /// <summary>
        /// 运行服务
        /// </summary>
        public static void RunService(IHost host)
        {
            try
            {
                var service = new WindowsServiceHost(host);
                ServiceBase.Run(service);
            }
            catch (Exception ex)
            {
                var logger = host.Services.GetService<ILogger<WindowsServiceHost>>();
                logger?.LogError(ex, "运行Windows服务时发生异常");
                throw;
            }
        }
    }
    
    /// <summary>
    /// Windows服务生命周期管理
    /// </summary>
    public class WindowsServiceLifetime : IHostLifetime
    {
        private readonly ILogger<WindowsServiceLifetime> _logger;
        private readonly IHostApplicationLifetime _applicationLifetime;
        private readonly TaskCompletionSource<object> _delayStart = new();
        
        public WindowsServiceLifetime(ILogger<WindowsServiceLifetime> logger, IHostApplicationLifetime applicationLifetime)
        {
            _logger = logger;
            _applicationLifetime = applicationLifetime;
        }
        
        public Task WaitForStartAsync(CancellationToken cancellationToken)
        {
            cancellationToken.Register(() => _delayStart.TrySetCanceled());
            _applicationLifetime.ApplicationStopping.Register(() => _delayStart.TrySetResult(null));
            
            new Thread(Run) { IsBackground = true }.Start();
            
            return _delayStart.Task;
        }
        
        public Task StopAsync(CancellationToken cancellationToken)
        {
            return Task.CompletedTask;
        }
        
        private void Run()
        {
            try
            {
                _logger.LogInformation("启动Windows服务生命周期管理");
                
                // 这里可以添加服务特定的初始化逻辑
                _delayStart.TrySetResult(null);
                
                _applicationLifetime.ApplicationStopping.WaitHandle.WaitOne();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Windows服务生命周期管理发生异常");
                _delayStart.TrySetException(ex);
            }
        }
    }
    
    /// <summary>
    /// Windows服务扩展方法
    /// </summary>
    public static class WindowsServiceExtensions
    {
        /// <summary>
        /// 配置为Windows服务
        /// </summary>
        public static IHostBuilder UseWindowsService(this IHostBuilder hostBuilder)
        {
            return hostBuilder.ConfigureServices((context, services) =>
            {
                services.AddSingleton<IHostLifetime, WindowsServiceLifetime>();
            });
        }
        
        /// <summary>
        /// 判断是否作为Windows服务运行
        /// </summary>
        public static bool IsWindowsService()
        {
            try
            {
                // 检查是否有控制台窗口
                return Environment.UserInteractive == false;
            }
            catch
            {
                return false;
            }
        }
        
        /// <summary>
        /// 检查命令行参数是否包含服务相关参数
        /// </summary>
        public static bool HasServiceArgument(string[] args)
        {
            if (args == null || args.Length == 0)
                return false;
                
            foreach (var arg in args)
            {
                if (string.Equals(arg, "--service", StringComparison.OrdinalIgnoreCase) ||
                    string.Equals(arg, "/service", StringComparison.OrdinalIgnoreCase) ||
                    string.Equals(arg, "-service", StringComparison.OrdinalIgnoreCase))
                {
                    return true;
                }
            }
            
            return false;
        }
    }
}