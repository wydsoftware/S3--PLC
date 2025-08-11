using System;
using System.Net.Sockets;
using Modbus.Device;
using System.Threading.Tasks;

class Program
{
    static async Task Main(string[] args)
    {
        string ipAddress = "192.168.1.2";
        int port = 502;
        
        Console.WriteLine($"连接到PLC: {ipAddress}:{port}");
        
        try
        {
            using var tcpClient = new TcpClient();
            await tcpClient.ConnectAsync(ipAddress, port);
            
            var modbusClient = ModbusIpMaster.CreateIp(tcpClient);
            
            Console.WriteLine("连接成功！开始读取数据...");
            
            // 测试不同的地址映射方式
            Console.WriteLine("\n=== 方式1: D地址-1 (当前程序使用的方式) ===");
            await TestAddressMapping(modbusClient, "D地址-1", addr => (ushort)(addr - 1));
            
            Console.WriteLine("\n=== 方式2: D地址直接映射 ===");
            await TestAddressMapping(modbusClient, "D地址直接", addr => (ushort)addr);
            
            Console.WriteLine("\n=== 方式3: D地址+40000 (标准Modbus映射) ===");
            await TestAddressMapping(modbusClient, "D地址+40000", addr => (ushort)(addr + 40000));
            
            Console.WriteLine("\n=== 方式4: D地址+39999 ===");
            await TestAddressMapping(modbusClient, "D地址+39999", addr => (ushort)(addr + 39999));
            
            // 尝试读取一些常见的测试地址
            Console.WriteLine("\n=== 扫描常见地址范围 ===");
            await ScanAddressRange(modbusClient, 0, 50);
            await ScanAddressRange(modbusClient, 800, 850);
            await ScanAddressRange(modbusClient, 40000, 40050);
            
        }
        catch (Exception ex)
        {
            Console.WriteLine($"连接失败: {ex.Message}");
        }
        
        Console.WriteLine("\n测试完成。");
    }
    
    static async Task TestAddressMapping(IModbusMaster modbusClient, string mappingName, Func<int, ushort> addressMapper)
    {
        int[] testAddresses = { 802, 804, 806, 808, 810 };
        
        foreach (int dAddress in testAddresses)
        {
            try
            {
                ushort modbusAddress = addressMapper(dAddress);
                ushort[] result = modbusClient.ReadHoldingRegisters(1, modbusAddress, 1);
                
                if (result[0] != 0)
                {
                    Console.WriteLine($"*** 找到非零值! {mappingName}: D{dAddress} -> Modbus{modbusAddress} = {result[0]} ***");
                }
                else
                {
                    Console.WriteLine($"{mappingName}: D{dAddress} -> Modbus{modbusAddress} = {result[0]}");
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"{mappingName}: D{dAddress} 读取失败: {ex.Message}");
            }
        }
    }
    
    static async Task ScanAddressRange(IModbusMaster modbusClient, int startAddr, int endAddr)
    {
        Console.WriteLine($"扫描地址范围 {startAddr}-{endAddr}:");
        int nonZeroCount = 0;
        
        for (int addr = startAddr; addr <= endAddr; addr++)
        {
            try
            {
                ushort[] result = modbusClient.ReadHoldingRegisters(1, (ushort)addr, 1);
                if (result[0] != 0)
                {
                    Console.WriteLine($"*** 地址 {addr} = {result[0]} ***");
                    nonZeroCount++;
                }
            }
            catch
            {
                // 忽略读取失败的地址
            }
        }
        
        if (nonZeroCount == 0)
        {
            Console.WriteLine($"地址范围 {startAddr}-{endAddr} 中所有值都是0或无法读取");
        }
    }
}