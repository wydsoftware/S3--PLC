using Microsoft.Extensions.Logging;
using System;
using System.Diagnostics;
using System.IO;
using System.ServiceProcess;
using System.Threading.Tasks;

namespace S3PLCDataCollector.Services
{
    /// <summary>
    /// Windows服务安装和管理工具类
    /// </summary>
    public class WindowsServiceInstaller
    {
        private readonly ILogger<WindowsServiceInstaller> _logger;
        private const string ServiceName = "S3PLCDataCollectorService";
        private const string ServiceDisplayName = "汇川S3 PLC数据采集服务";
        private const string ServiceDescription = "自动采集汇川S3 PLC数据并保存到数据库";
        
        public WindowsServiceInstaller(ILogger<WindowsServiceInstaller> logger)
        {
            _logger = logger;
        }
        
        /// <summary>
        /// 检查服务是否已安装
        /// </summary>
        public bool IsServiceInstalled()
        {
            try
            {
                using var service = new ServiceController(ServiceName);
                var status = service.Status; // 如果服务不存在会抛出异常
                return true;
            }
            catch (InvalidOperationException)
            {
                return false;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "检查服务安装状态时发生异常");
                return false;
            }
        }
        
        /// <summary>
        /// 获取服务状态
        /// </summary>
        public ServiceControllerStatus? GetServiceStatus()
        {
            try
            {
                if (!IsServiceInstalled())
                    return null;
                    
                using var service = new ServiceController(ServiceName);
                return service.Status;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "获取服务状态时发生异常");
                return null;
            }
        }
        
        /// <summary>
        /// 安装Windows服务
        /// </summary>
        public async Task<bool> InstallServiceAsync()
        {
            try
            {
                if (IsServiceInstalled())
                {
                    _logger.LogWarning("服务已经安装");
                    return true;
                }
                
                var executablePath = GetExecutablePath();
                if (!File.Exists(executablePath))
                {
                    _logger.LogError("找不到可执行文件: {ExecutablePath}", executablePath);
                    return false;
                }
                
                // 使用sc命令安装服务
                var arguments = $"create \"{ServiceName}\" binPath= \"{executablePath} --service\" " +
                              $"DisplayName= \"{ServiceDisplayName}\" " +
                              $"start= auto " +
                              $"depend= Tcpip";
                
                var result = await RunCommandAsync("sc", arguments);
                
                if (result.Success)
                {
                    // 设置服务描述
                    await RunCommandAsync("sc", $"description \"{ServiceName}\" \"{ServiceDescription}\"");
                    
                    _logger.LogInformation("Windows服务安装成功");
                    return true;
                }
                else
                {
                    _logger.LogError("Windows服务安装失败: {Error}", result.Error);
                    return false;
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "安装Windows服务时发生异常");
                return false;
            }
        }
        
        /// <summary>
        /// 卸载Windows服务
        /// </summary>
        public async Task<bool> UninstallServiceAsync()
        {
            try
            {
                if (!IsServiceInstalled())
                {
                    _logger.LogWarning("服务未安装");
                    return true;
                }
                
                // 先停止服务
                await StopServiceAsync();
                
                // 卸载服务
                var result = await RunCommandAsync("sc", $"delete \"{ServiceName}\"");
                
                if (result.Success)
                {
                    _logger.LogInformation("Windows服务卸载成功");
                    return true;
                }
                else
                {
                    _logger.LogError("Windows服务卸载失败: {Error}", result.Error);
                    return false;
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "卸载Windows服务时发生异常");
                return false;
            }
        }
        
        /// <summary>
        /// 启动服务
        /// </summary>
        public async Task<bool> StartServiceAsync()
        {
            try
            {
                if (!IsServiceInstalled())
                {
                    _logger.LogError("服务未安装，无法启动");
                    return false;
                }
                
                using var service = new ServiceController(ServiceName);
                
                if (service.Status == ServiceControllerStatus.Running)
                {
                    _logger.LogInformation("服务已经在运行中");
                    return true;
                }
                
                if (service.Status == ServiceControllerStatus.StartPending)
                {
                    _logger.LogInformation("服务正在启动中，等待完成...");
                    await WaitForServiceStatusAsync(service, ServiceControllerStatus.Running, TimeSpan.FromSeconds(30));
                    return service.Status == ServiceControllerStatus.Running;
                }
                
                _logger.LogInformation("正在启动服务...");
                service.Start();
                
                await WaitForServiceStatusAsync(service, ServiceControllerStatus.Running, TimeSpan.FromSeconds(30));
                
                if (service.Status == ServiceControllerStatus.Running)
                {
                    _logger.LogInformation("服务启动成功");
                    return true;
                }
                else
                {
                    _logger.LogError("服务启动失败，当前状态: {Status}", service.Status);
                    return false;
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "启动服务时发生异常");
                return false;
            }
        }
        
        /// <summary>
        /// 停止服务
        /// </summary>
        public async Task<bool> StopServiceAsync()
        {
            try
            {
                if (!IsServiceInstalled())
                {
                    _logger.LogWarning("服务未安装");
                    return true;
                }
                
                using var service = new ServiceController(ServiceName);
                
                if (service.Status == ServiceControllerStatus.Stopped)
                {
                    _logger.LogInformation("服务已经停止");
                    return true;
                }
                
                if (service.Status == ServiceControllerStatus.StopPending)
                {
                    _logger.LogInformation("服务正在停止中，等待完成...");
                    await WaitForServiceStatusAsync(service, ServiceControllerStatus.Stopped, TimeSpan.FromSeconds(30));
                    return service.Status == ServiceControllerStatus.Stopped;
                }
                
                if (service.CanStop)
                {
                    _logger.LogInformation("正在停止服务...");
                    service.Stop();
                    
                    await WaitForServiceStatusAsync(service, ServiceControllerStatus.Stopped, TimeSpan.FromSeconds(30));
                    
                    if (service.Status == ServiceControllerStatus.Stopped)
                    {
                        _logger.LogInformation("服务停止成功");
                        return true;
                    }
                    else
                    {
                        _logger.LogError("服务停止失败，当前状态: {Status}", service.Status);
                        return false;
                    }
                }
                else
                {
                    _logger.LogWarning("服务不支持停止操作");
                    return false;
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "停止服务时发生异常");
                return false;
            }
        }
        
        /// <summary>
        /// 重启服务
        /// </summary>
        public async Task<bool> RestartServiceAsync()
        {
            try
            {
                _logger.LogInformation("正在重启服务...");
                
                var stopResult = await StopServiceAsync();
                if (!stopResult)
                {
                    _logger.LogError("停止服务失败，无法重启");
                    return false;
                }
                
                // 等待一段时间确保服务完全停止
                await Task.Delay(2000);
                
                var startResult = await StartServiceAsync();
                if (startResult)
                {
                    _logger.LogInformation("服务重启成功");
                    return true;
                }
                else
                {
                    _logger.LogError("启动服务失败，重启失败");
                    return false;
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "重启服务时发生异常");
                return false;
            }
        }
        
        /// <summary>
        /// 等待服务状态变化
        /// </summary>
        private async Task WaitForServiceStatusAsync(ServiceController service, ServiceControllerStatus targetStatus, TimeSpan timeout)
        {
            var startTime = DateTime.Now;
            
            while (DateTime.Now - startTime < timeout)
            {
                service.Refresh();
                if (service.Status == targetStatus)
                    return;
                    
                await Task.Delay(500);
            }
        }
        
        /// <summary>
        /// 获取可执行文件路径
        /// </summary>
        private string GetExecutablePath()
        {
            var currentPath = System.Reflection.Assembly.GetExecutingAssembly().Location;
            return Path.ChangeExtension(currentPath, ".exe");
        }
        
        /// <summary>
        /// 运行命令行命令
        /// </summary>
        private async Task<CommandResult> RunCommandAsync(string fileName, string arguments)
        {
            try
            {
                using var process = new Process();
                process.StartInfo.FileName = fileName;
                process.StartInfo.Arguments = arguments;
                process.StartInfo.UseShellExecute = false;
                process.StartInfo.RedirectStandardOutput = true;
                process.StartInfo.RedirectStandardError = true;
                process.StartInfo.CreateNoWindow = true;
                
                _logger.LogDebug("执行命令: {FileName} {Arguments}", fileName, arguments);
                
                process.Start();
                
                var output = await process.StandardOutput.ReadToEndAsync();
                var error = await process.StandardError.ReadToEndAsync();
                
                await process.WaitForExitAsync();
                
                var success = process.ExitCode == 0;
                
                _logger.LogDebug("命令执行完成，退出码: {ExitCode}", process.ExitCode);
                
                return new CommandResult
                {
                    Success = success,
                    Output = output,
                    Error = error,
                    ExitCode = process.ExitCode
                };
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "执行命令时发生异常: {FileName} {Arguments}", fileName, arguments);
                return new CommandResult
                {
                    Success = false,
                    Error = ex.Message,
                    ExitCode = -1
                };
            }
        }
        
        /// <summary>
        /// 获取服务状态信息
        /// </summary>
        public string GetServiceStatusInfo()
        {
            try
            {
                if (!IsServiceInstalled())
                    return "服务未安装";
                    
                var status = GetServiceStatus();
                return status switch
                {
                    ServiceControllerStatus.Running => "运行中",
                    ServiceControllerStatus.Stopped => "已停止",
                    ServiceControllerStatus.StartPending => "启动中",
                    ServiceControllerStatus.StopPending => "停止中",
                    ServiceControllerStatus.Paused => "已暂停",
                    ServiceControllerStatus.PausePending => "暂停中",
                    ServiceControllerStatus.ContinuePending => "恢复中",
                    _ => "未知状态"
                };
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "获取服务状态信息时发生异常");
                return "状态异常";
            }
        }
    }
    
    /// <summary>
    /// 命令执行结果
    /// </summary>
    public class CommandResult
    {
        public bool Success { get; set; }
        public string Output { get; set; } = string.Empty;
        public string Error { get; set; } = string.Empty;
        public int ExitCode { get; set; }
    }
}