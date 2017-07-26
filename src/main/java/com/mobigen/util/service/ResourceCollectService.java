package com.mobigen.util.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mobigen.util.dao.ResourceCollectDAO;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.hardware.CentralProcessor.TickType;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;

@Service
public class ResourceCollectService {
	
	private static final Logger logger = LoggerFactory.getLogger(ResourceCollectService.class);
	
	@Autowired
	private ResourceCollectDAO dao;
	
	@Value("${server.ip}")
	private String serverIp;

	public void getResource() throws Exception {
		Map<String,String> resultMap = getSystemResource();
		dao.insertResource(resultMap);
	}
	
	public void getSystem() throws Exception {
		
		Map<String,String> resultMap = getSystemResource();
		dao.insertResource(resultMap);
	}
	
	private Map<String, String> getSystemResource() throws Exception {
		Map<String, String> resourceMap = new HashMap<String, String>();
		resourceMap.put("cpu_usage", "0");
		resourceMap.put("ram_usage", "0");
		resourceMap.put("disk_usage", "0");
		resourceMap.put("network_receive", "0");
		resourceMap.put("network_send", "0");
		resourceMap.put("iowait", "0");
		resourceMap.put("load_avg", "0");
		resourceMap.put("ip", serverIp);
		resourceMap.put("datetime", new SimpleDateFormat("yyyyMMddHHmm00").format(new Date()));
		
		SystemInfo si = new SystemInfo();

        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();
		
        int memoryUsage = printMemory(hal.getMemory());
        int cpuUsage = printCpu(hal.getProcessor());
        int diskUsage = printFileSystem(os.getFileSystem());
        long[] networkUsage = printNetworkInterfaces(hal.getNetworkIFs());
        double[] cpuStatus = printCpuStatus(hal.getProcessor());
        
        // threshold over check
        checkThreshold(cpuUsage, memoryUsage, diskUsage, serverIp);
        
        // network variance check
        Map<String,Object> prevResourceMap = dao.selectPrevResource(serverIp);
        
        long prevNetReceive = (Long) prevResourceMap.get("NET_RECEIVE");
        long prevNetSend = (Long) prevResourceMap.get("NET_SEND");
                
        resourceMap.put("cpu_usage", String.valueOf(cpuUsage));
        resourceMap.put("ram_usage", String.valueOf(memoryUsage));
		resourceMap.put("disk_usage", String.valueOf(diskUsage));
		resourceMap.put("network_receive", String.valueOf(networkUsage[0]));
		resourceMap.put("network_send", String.valueOf(networkUsage[1]));
		resourceMap.put("network_receive_variance", String.valueOf( networkUsage[0] - prevNetReceive));
		resourceMap.put("network_send_variance", String.valueOf( networkUsage[1] - prevNetSend));
		resourceMap.put("iowait", String.format("%.2f",cpuStatus[0]));
		resourceMap.put("load_avg", String.format("%.2f",cpuStatus[1]));
				
		return resourceMap;
	}
	
	private void checkThreshold(int cpuUsage, int memoryUsage, int diskUsage, String serverIp) throws Exception {
		
		Map<String, Integer> nodeThresholdMap = dao.selectThresholdByServerIp(serverIp);
		int nodeId = nodeThresholdMap.get("NODE_ID");
        int cpuThreshold = nodeThresholdMap.get("CPU_THRESHOLD");
        int memoryThreshold = nodeThresholdMap.get("MEMORY_THRESHOLD");
        int diskThreshold = nodeThresholdMap.get("DISK_THRESHOLD");
        
        List<Map<String, Object>> thresholdOverList = new ArrayList<>();
        Map<String,Object> thresholdOverMap = null;
        
        // cpu
        if(cpuUsage > cpuThreshold) {
        	thresholdOverMap = getThresholdOverMap(nodeId);
        	thresholdOverMap.put("alertMessage", "CPU_USAGE(" + cpuUsage + ")");
        	thresholdOverList.add(thresholdOverMap);
        }
        // memory
        if(memoryUsage > memoryThreshold) {
        	thresholdOverMap = getThresholdOverMap(nodeId);
        	thresholdOverMap.put("alertMessage", "MEMORY_USAGE(" + memoryUsage + ")");
        	thresholdOverList.add(thresholdOverMap);
        }
        // disk
        if(diskUsage > diskThreshold) {
        	thresholdOverMap = getThresholdOverMap(nodeId);
        	thresholdOverMap.put("alertMessage", "DISK_USAGE(" + diskUsage + ")");
        	thresholdOverList.add(thresholdOverMap);
        }
        
        // cpu, memory, disk 의 임계치를 넘어가는 정보를 DB에 저장한다.
        if(thresholdOverList.size() > 0) {
        	dao.insertAlert(thresholdOverList);
        }
	}
	
	private Map<String,Object> getThresholdOverMap(int nodeId) {
		Map<String,Object> thresholdOverMap = new HashMap<>();
		thresholdOverMap.put("nodeId", nodeId);
    	thresholdOverMap.put("updateTime", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
    	thresholdOverMap.put("alertType", "RESOURCE");
    	thresholdOverMap.put("alertLevel", "WARN");
    	return thresholdOverMap;
	}

	/**
	 * Memory Usage
	 * @param memory
	 * @return
	 */
    private int printMemory(GlobalMemory memory) {
    	float usedMemory = 100F * memory.getAvailable() / memory.getTotal();
    	return 100 - Math.round(usedMemory);
    }

    /**
     * Cpu Usage
     * @param processor
     */
    private int printCpu(CentralProcessor processor) {
    	float usedCpu = (float)(processor.getSystemCpuLoadBetweenTicks() * 100);
    	return Math.round(usedCpu);
    }
    
    private double[] printCpuStatus(CentralProcessor processor) {
    	
    	double[] result = new double[2];
    	
    	long[] prevTicks = processor.getSystemCpuLoadTicks();
    	// Wait a second...
        Util.sleep(1000);
    	long[] ticks = processor.getSystemCpuLoadTicks();
    	
    	long user = ticks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
        long nice = ticks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
        long sys = ticks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
        long idle = ticks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];
        long iowait = ticks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
        long irq = ticks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
        long softirq = ticks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
        long steal = ticks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];
        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;
        
        result[0] = 100d * iowait / totalCpu;
        
        double[] loadAverage = processor.getSystemLoadAverage(3);
        result[1] = loadAverage[0] < 0 ? 0.0 : loadAverage[0];
        
        return result;
    }

    /**
     * Disk Usage
     * @param fileSystem
     * @return
     */
    private int printFileSystem(FileSystem fileSystem) {
        OSFileStore[] fsArray = fileSystem.getFileStores();
        long total = 0;
        long usable = 0;
        for (OSFileStore fs : fsArray) {
            usable += fs.getUsableSpace();
            total += fs.getTotalSpace();
        }
        
        float usage = (float)(100d * (total - usable) / total);
        return Math.round(usage);
    }
    
    private long[] printNetworkInterfaces(NetworkIF[] networkIFs) {
    	
    	long[] result = new long[2];
    	long receive = 0L;
    	long send = 0L;
    	        
    	for (NetworkIF net : networkIFs) {
    		receive += net.getBytesRecv();
    		send += net.getBytesSent();
        }
    	
    	result[0] = receive;
    	result[1] = send;
    	
    	return result;
    }
}
