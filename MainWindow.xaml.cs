using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using S3PLCDataCollector.Models;
using S3PLCDataCollector.Services;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Threading;
using System.Drawing;
using System.Windows.Forms;

namespace S3PLCDataCollector
{
    /// <summary>
    /// MainWindow.xaml 的交互逻辑
    /// </summary>
    public partial class MainWindow : Window
    {
        private DataCollectionService? _dataCollectionService;
        private ILogger<MainWindow> _logger;
        private readonly DispatcherTimer _uiUpdateTimer;
        private readonly DispatcherTimer _timeUpdateTimer;
        private readonly ObservableCollection<DeviceStatus> _deviceStatusCollection;
        private NotifyIcon? _notifyIcon;
        private bool _isClosing = false;
        
        // 默认构造函数（用于XAML）
        public MainWindow()
        {
            InitializeComponent();
            _deviceStatusCollection = new ObservableCollection<DeviceStatus>();
            
            // 初始化默认logger
            var loggerFactory = LoggerFactory.Create(builder => builder.AddConsole());
            _logger = loggerFactory.CreateLogger<MainWindow>();
            
            // 绑定数据源
            dgDeviceData.ItemsSource = _deviceStatusCollection;
            
            // 初始化定时器
            _uiUpdateTimer = new DispatcherTimer
            {
                Interval = TimeSpan.FromSeconds(1)
            };
            _uiUpdateTimer.Tick += UiUpdateTimer_Tick;
            
            _timeUpdateTimer = new DispatcherTimer
            {
                Interval = TimeSpan.FromSeconds(1)
            };
            _timeUpdateTimer.Tick += TimeUpdateTimer_Tick;
        }
        
        /// <summary>
        /// 初始化依赖注入的服务
        /// </summary>
        public void InitializeServices(DataCollectionService dataCollectionService, ILogger<MainWindow> logger)
        {
            _dataCollectionService = dataCollectionService;
            _logger = logger;
            
            // 订阅事件
            _dataCollectionService.DataCollected += OnDataCollected;
            _dataCollectionService.ConnectionStatusChanged += OnConnectionStatusChanged;
            
            // 启动定时器
            _uiUpdateTimer.Start();
            _timeUpdateTimer.Start();
            
            // 初始化其他组件
            InitializeNotifyIcon();
            InitializeDeviceData();
            
            // 异步初始化
            _ = Task.Run(InitializeAsync);
            
            _logger.LogInformation("MainWindow服务初始化完成");
        }
        
        /// <summary>
         /// 初始化设备数据
         /// </summary>
         private void InitializeDeviceData()
         {
             // 这里可以添加初始化设备数据的逻辑
         }
         

        
        /// <summary>
        /// 初始化系统托盘
        /// </summary>
        private void InitializeNotifyIcon()
        {
            try
            {
                _notifyIcon = new NotifyIcon
                {
                    Text = "汇川S3 PLC数据采集系统 - 点击查看状态",
                    Visible = true
                };
                
                // 设置托盘图标（使用默认图标，后续可以替换为自定义图标）
                _notifyIcon.Icon = SystemIcons.Application;
                
                // 双击托盘图标显示窗口
                _notifyIcon.DoubleClick += (s, e) => ShowWindow();
                
                // 单击显示状态信息
                _notifyIcon.Click += NotifyIcon_Click;
                
                // 创建右键菜单
                UpdateTrayContextMenu();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "初始化系统托盘失败");
            }
        }
        
        /// <summary>
        /// 更新托盘右键菜单
        /// </summary>
        private void UpdateTrayContextMenu()
        {
            if (_notifyIcon == null) return;
            
            var contextMenu = new ContextMenuStrip();
            
            // 显示窗口
            contextMenu.Items.Add("显示主窗口", null, (s, e) => ShowWindow());
            contextMenu.Items.Add("-");
            
            // 采集控制
            var isCollecting = _dataCollectionService.IsRunning;
            if (isCollecting)
            {
                contextMenu.Items.Add("⏸️ 暂停采集", null, (s, e) => StopCollection());
            }
            else
            {
                contextMenu.Items.Add("▶️ 启动采集", null, (s, e) => _ = StartCollectionAsync());
            }
            
            contextMenu.Items.Add("-");
            
            // 状态信息
            var statusItem = new ToolStripMenuItem("📊 采集状态");
            statusItem.DropDownItems.Add($"连接状态: {(isCollecting ? "已连接" : "未连接")}");
            statusItem.DropDownItems.Add($"采集状态: {(isCollecting ? "运行中" : "已停止")}");
            statusItem.DropDownItems.Add($"最后更新: {DateTime.Now:HH:mm:ss}");
            contextMenu.Items.Add(statusItem);
            
            // 配置选项
            var configItem = new ToolStripMenuItem("⚙️ 快速配置");
            configItem.DropDownItems.Add("PLC IP设置", null, (s, e) => ShowPLCConfigDialog());
            configItem.DropDownItems.Add("采集间隔设置", null, (s, e) => ShowIntervalConfigDialog());
            contextMenu.Items.Add(configItem);
            
            contextMenu.Items.Add("-");
            contextMenu.Items.Add("❌ 退出程序", null, (s, e) => ExitApplication());
            
            _notifyIcon.ContextMenuStrip = contextMenu;
        }
        
        /// <summary>
        /// 托盘图标单击事件
        /// </summary>
        private void NotifyIcon_Click(object? sender, EventArgs e)
        {
            var mouseEvent = e as MouseEventArgs;
            if (mouseEvent?.Button == MouseButtons.Left)
            {
                // 左键单击显示状态气泡
                var status = _dataCollectionService.IsRunning ? "运行中" : "已停止";
                var message = $"采集状态: {status}\n最后更新: {DateTime.Now:HH:mm:ss}";
                _notifyIcon?.ShowBalloonTip(3000, "S3 PLC数据采集系统", message, ToolTipIcon.Info);
            }
        }
        
        /// <summary>
        /// 异步初始化
        /// </summary>
        private async Task InitializeAsync()
        {
            try
            {
                await Dispatcher.InvokeAsync(() =>
                {
                    AddLogMessage("正在初始化系统...");
                    txtStatusMessage.Text = "初始化中...";
                });
                
                // 刷新设备数据
                await RefreshDeviceDataAsync();
                
                await Dispatcher.InvokeAsync(() =>
                {
                    AddLogMessage("系统初始化完成");
                    txtStatusMessage.Text = "就绪";
                    
                    // 更新界面状态
                    UpdateUIStatus();
                });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "异步初始化失败");
                await Dispatcher.InvokeAsync(() =>
                {
                    AddLogMessage($"初始化失败: {ex.Message}");
                    txtStatusMessage.Text = "初始化失败";
                });
            }
        }
        
        /// <summary>
        /// UI更新定时器事件
        /// </summary>
        private async void UiUpdateTimer_Tick(object? sender, EventArgs e)
        {
            try
            {
                // 更新托盘菜单状态
                UpdateTrayContextMenu();
                
                // 更新托盘图标提示文本
                if (_notifyIcon != null)
                {
                    var status = _dataCollectionService.IsRunning ? "运行中" : "已停止";
                    _notifyIcon.Text = $"汇川S3 PLC数据采集系统 - {status}";
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "更新托盘状态失败");
            }
            
            try
            {
                UpdateUIStatus();
                
                // 每10秒刷新一次设备数据
                if (DateTime.Now.Second % 10 == 0)
                {
                    await RefreshDeviceDataAsync();
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "UI更新定时器异常");
            }
        }
        
        /// <summary>
        /// 时间更新定时器事件
        /// </summary>
        private void TimeUpdateTimer_Tick(object? sender, EventArgs e)
        {
            txtCurrentTime.Text = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss");
        }
        
        /// <summary>
        /// 更新UI状态
        /// </summary>
        private void UpdateUIStatus()
        {
            try
            {
                // 更新采集状态
                if (_dataCollectionService.IsRunning)
                {
                    txtCollectionStatus.Text = "运行中";
                    txtCollectionStatus.Foreground = System.Windows.Media.Brushes.Green;
                    btnStart.IsEnabled = false;
                    btnStop.IsEnabled = true;
                }
                else
                {
                    txtCollectionStatus.Text = "已停止";
                    txtCollectionStatus.Foreground = System.Windows.Media.Brushes.Orange;
                    btnStart.IsEnabled = true;
                    btnStop.IsEnabled = false;
                }
                
                // 更新设备计数
                var connectedCount = _deviceStatusCollection.Count(d => d.ConnectionStatus == "已连接");
                txtDeviceCount.Text = $"设备: {connectedCount}/{_deviceStatusCollection.Count}";
                
                // 更新读取间隔显示
                txtReadInterval.Text = _dataCollectionService.ReadIntervalSeconds.ToString();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "更新UI状态失败");
            }
        }
        
        /// <summary>
        /// 数据采集完成事件处理
        /// </summary>
        private async void OnDataCollected(object? sender, DataCollectionEventArgs e)
        {
            await Dispatcher.InvokeAsync(() =>
            {
                try
                {
                    // 更新最后更新时间
                    txtLastUpdate.Text = e.EndTime.ToString("HH:mm:ss");
                    
                    // 添加日志
                    if (e.IsSuccess)
                    {
                        AddLogMessage($"[{e.EndTime:HH:mm:ss}] 成功采集 {e.CollectedData.Count} 个设备数据，耗时 {e.Duration.TotalMilliseconds:F0}ms");
                        txtStatusMessage.Text = $"最后采集: {e.EndTime:HH:mm:ss}";
                    }
                    else
                    {
                        var errorMsg = string.Join("; ", e.Errors);
                        AddLogMessage($"[{e.EndTime:HH:mm:ss}] 采集失败: {errorMsg}");
                        txtStatusMessage.Text = "采集失败";
                    }
                    
                    // 刷新设备数据显示
                    _ = Task.Run(RefreshDeviceDataAsync);
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "处理数据采集事件失败");
                }
            });
        }
        
        /// <summary>
        /// 连接状态变化事件处理
        /// </summary>
        private async void OnConnectionStatusChanged(object? sender, ConnectionStatusEventArgs e)
        {
            await Dispatcher.InvokeAsync(() =>
            {
                try
                {
                    if (e.IsConnected)
                    {
                        txtConnectionStatus.Text = "已连接";
                        txtConnectionStatus.Foreground = System.Windows.Media.Brushes.Green;
                        AddLogMessage($"[{e.Timestamp:HH:mm:ss}] PLC连接成功: {e.StatusMessage}");
                    }
                    else
                    {
                        txtConnectionStatus.Text = "未连接";
                        txtConnectionStatus.Foreground = System.Windows.Media.Brushes.Red;
                        AddLogMessage($"[{e.Timestamp:HH:mm:ss}] PLC连接失败: {e.StatusMessage}");
                    }
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "处理连接状态变化事件失败");
                }
            });
        }
        
        /// <summary>
        /// 刷新设备数据
        /// </summary>
        private async Task RefreshDeviceDataAsync()
        {
            try
            {
                var deviceStatusList = await _dataCollectionService.GetCurrentDeviceStatusAsync();
                
                await Dispatcher.InvokeAsync(() =>
                {
                    _deviceStatusCollection.Clear();
                    foreach (var status in deviceStatusList)
                    {
                        _deviceStatusCollection.Add(status);
                    }
                });
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "刷新设备数据失败");
                await Dispatcher.InvokeAsync(() =>
                {
                    AddLogMessage($"刷新设备数据失败: {ex.Message}");
                });
            }
        }
        
        /// <summary>
        /// 添加日志消息
        /// </summary>
        private void AddLogMessage(string message)
        {
            try
            {
                var timestamp = DateTime.Now.ToString("HH:mm:ss");
                var logMessage = $"[{timestamp}] {message}\n";
                
                txtLog.Text += logMessage;
                
                // 限制日志长度
                if (txtLog.Text.Length > 10000)
                {
                    var lines = txtLog.Text.Split('\n');
                    txtLog.Text = string.Join("\n", lines.Skip(lines.Length / 2));
                }
                
                // 自动滚动到底部
                if (chkAutoScroll.IsChecked == true)
                {
                    logScrollViewer.ScrollToEnd();
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "添加日志消息失败");
            }
        }
        
        /// <summary>
        /// 启动采集按钮点击事件
        /// </summary>
        private async void BtnStart_Click(object sender, RoutedEventArgs e)
        {
            await StartCollectionAsync();
        }
        
        /// <summary>
        /// 启动数据采集
        /// </summary>
        private async Task StartCollectionAsync()
        {
            try
            {
                AddLogMessage("正在启动数据采集...");
                txtStatusMessage.Text = "启动中...";
                
                await _dataCollectionService.StartCollectionAsync();
                
                AddLogMessage("数据采集已启动");
                UpdateUIStatus();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "启动数据采集失败");
                AddLogMessage($"启动数据采集失败: {ex.Message}");
                txtStatusMessage.Text = "启动失败";
            }
        }
        
        /// <summary>
        /// 停止采集按钮点击事件
        /// </summary>
        private void BtnStop_Click(object sender, RoutedEventArgs e)
        {
            StopCollection();
        }
        
        /// <summary>
        /// 停止数据采集
        /// </summary>
        private void StopCollection()
        {
            try
            {
                AddLogMessage("正在停止数据采集...");
                txtStatusMessage.Text = "停止中...";
                
                _dataCollectionService.StopCollection();
                
                AddLogMessage("数据采集已停止");
                txtStatusMessage.Text = "已停止";
                UpdateUIStatus();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "停止数据采集失败");
                AddLogMessage($"停止数据采集失败: {ex.Message}");
            }
        }
        
        /// <summary>
        /// 刷新数据按钮点击事件
        /// </summary>
        private async void BtnRefresh_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                AddLogMessage("正在刷新设备数据...");
                await RefreshDeviceDataAsync();
                AddLogMessage("设备数据刷新完成");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "刷新设备数据失败");
                AddLogMessage($"刷新设备数据失败: {ex.Message}");
            }
        }
        
        /// <summary>
        /// 应用配置按钮点击事件
        /// </summary>
        private async void BtnApplyConfig_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                var ipAddress = txtPLCIP.Text.Trim();
                if (string.IsNullOrEmpty(ipAddress))
                {
                    System.Windows.MessageBox.Show("请输入有效的PLC IP地址", "配置错误", MessageBoxButton.OK, MessageBoxImage.Warning);
                    return;
                }
                
                if (!int.TryParse(txtPLCPort.Text.Trim(), out int port) || port <= 0 || port > 65535)
                {
                    System.Windows.MessageBox.Show("请输入有效的端口号(1-65535)", "配置错误", MessageBoxButton.OK, MessageBoxImage.Warning);
                    return;
                }
                
                if (!int.TryParse(txtReadInterval.Text.Trim(), out int interval) || interval <= 0)
                {
                    System.Windows.MessageBox.Show("请输入有效的读取间隔(大于0的整数)", "配置错误", MessageBoxButton.OK, MessageBoxImage.Warning);
                    return;
                }
                
                AddLogMessage("正在应用配置...");
                
                // 更新PLC配置
                await _dataCollectionService.UpdatePLCConfigAsync(ipAddress, port);
                
                // 更新读取间隔
                await _dataCollectionService.UpdateReadIntervalAsync(interval);
                
                AddLogMessage($"配置已更新: PLC={ipAddress}:{port}, 间隔={interval}秒");
                txtStatusMessage.Text = "配置已更新";
                
                System.Windows.MessageBox.Show("配置已成功更新", "配置更新", MessageBoxButton.OK, MessageBoxImage.Information);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "应用配置失败");
                AddLogMessage($"应用配置失败: {ex.Message}");
                System.Windows.MessageBox.Show($"应用配置失败: {ex.Message}", "配置错误", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }
        
        /// <summary>
        /// 清空日志按钮点击事件
        /// </summary>
        private void BtnClearLog_Click(object sender, RoutedEventArgs e)
        {
            txtLog.Text = "";
            AddLogMessage("日志已清空");
        }
        
        /// <summary>
        /// 窗口状态变化事件
        /// </summary>
        private void Window_StateChanged(object sender, EventArgs e)
        {
            if (WindowState == WindowState.Minimized)
            {
                Hide();
                _notifyIcon?.ShowBalloonTip(2000, "汇川S3 PLC数据采集系统", "程序已最小化到系统托盘", ToolTipIcon.Info);
            }
        }
        
        /// <summary>
        /// 窗口关闭事件
        /// </summary>
        private void Window_Closing(object sender, CancelEventArgs e)
        {
            if (!_isClosing)
            {
                e.Cancel = true;
                Hide();
                _notifyIcon?.ShowBalloonTip(2000, "汇川S3 PLC数据采集系统", "程序已最小化到系统托盘，双击图标可重新显示", ToolTipIcon.Info);
            }
        }
        
        /// <summary>
        /// 显示窗口
        /// </summary>
        private void ShowWindow()
        {
            Show();
            WindowState = WindowState.Normal;
            Activate();
        }
        
        /// <summary>
        /// 显示PLC IP配置对话框
        /// </summary>
        private void ShowPLCConfigDialog()
        {
            var currentIP = txtPLCIP?.Text ?? "192.168.1.2";
            var result = Microsoft.VisualBasic.Interaction.InputBox(
                "请输入PLC IP地址:", 
                "PLC IP配置", 
                currentIP);
            
            if (!string.IsNullOrEmpty(result) && result != currentIP)
            {
                Dispatcher.Invoke(() =>
                {
                    if (txtPLCIP != null)
                    {
                        txtPLCIP.Text = result;
                        BtnApplyConfig_Click(this, new RoutedEventArgs());
                    }
                });
                
                _notifyIcon?.ShowBalloonTip(2000, "配置更新", $"PLC IP已更新为: {result}", ToolTipIcon.Info);
            }
        }
        
        /// <summary>
        /// 显示采集间隔配置对话框
        /// </summary>
        private void ShowIntervalConfigDialog()
        {
            var currentInterval = txtReadInterval?.Text ?? "5";
            var result = Microsoft.VisualBasic.Interaction.InputBox(
                "请输入采集间隔(秒):", 
                "采集间隔配置", 
                currentInterval);
            
            if (!string.IsNullOrEmpty(result) && result != currentInterval)
            {
                if (int.TryParse(result, out int interval) && interval > 0)
                {
                    Dispatcher.Invoke(() =>
                    {
                        if (txtReadInterval != null)
                        {
                            txtReadInterval.Text = result;
                            BtnApplyConfig_Click(this, new RoutedEventArgs());
                        }
                    });
                    
                    _notifyIcon?.ShowBalloonTip(2000, "配置更新", $"采集间隔已更新为: {result}秒", ToolTipIcon.Info);
                }
                else
                {
                    _notifyIcon?.ShowBalloonTip(2000, "配置错误", "请输入有效的正整数", ToolTipIcon.Warning);
                }
            }
        }
        
        /// <summary>
        /// 退出应用程序
        /// </summary>
        private void ExitApplication()
        {
            var result = System.Windows.MessageBox.Show("确定要退出程序吗？这将停止数据采集。", "确认退出", 
                MessageBoxButton.YesNo, MessageBoxImage.Question);
            
            if (result == MessageBoxResult.Yes)
            {
                _isClosing = true;
                
                try
                {
                    // 停止数据采集
                    _dataCollectionService.StopCollection();
                    
                    // 停止定时器
                    _uiUpdateTimer?.Stop();
                    _timeUpdateTimer?.Stop();
                    
                    // 清理系统托盘
                    _notifyIcon?.Dispose();
                    
                    _logger.LogInformation("程序正常退出");
                }
                catch (Exception ex)
                {
                    _logger.LogError(ex, "程序退出时发生异常");
                }
                
                System.Windows.Application.Current.Shutdown();
            }
        }
        
        /// <summary>
        /// 窗口析构
        /// </summary>
        protected override void OnClosed(EventArgs e)
        {
            try
            {
                _uiUpdateTimer?.Stop();
                _timeUpdateTimer?.Stop();
                _notifyIcon?.Dispose();
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "窗口关闭清理资源时发生异常");
            }
            
            base.OnClosed(e);
        }
    }
}