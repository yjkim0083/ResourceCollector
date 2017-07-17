package com.mobigen.util.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

@Service
public class ResourceCollectService {
	
	private static final Logger logger = LoggerFactory.getLogger(ResourceCollectService.class);
	
	@Autowired
	private ResourceCollectDAO dao;
	
	@Value("${server.ip}")
	private String serverIp;

	public Map<String,String> getResource() throws Exception {
		
		Map<String,String> resultMap = getSystemResource();
		logger.info(resultMap.toString());
		//dao.insertResource(resultMap);
		
		return resultMap;
	}
	
	private Map<String, String> getSystemResource() {
		Map<String, String> resourceMap = new HashMap<String, String>();
		resourceMap.put("cpu_usage", "");
		resourceMap.put("ram_usage", "");
		resourceMap.put("disk_usage", "");
		resourceMap.put("datetime", new SimpleDateFormat("yyyyMMddHHmm00").format(new Date()));
		
		SystemInfo si = new SystemInfo();

        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();
		
        int memoryUsage = printMemory(hal.getMemory());
        int cpuUsage = printCpu(hal.getProcessor());
        int diskUsage = printFileSystem(os.getFileSystem());
        long[] networkUsage = printNetworkInterfaces(hal.getNetworkIFs());
        
        resourceMap.put("cpu_usage", String.valueOf(cpuUsage));
        resourceMap.put("ram_usage", String.valueOf(memoryUsage));
		resourceMap.put("disk_usage", String.valueOf(diskUsage));
		resourceMap.put("network_receive", String.valueOf(networkUsage[0]));
		resourceMap.put("network_send", String.valueOf(networkUsage[1]));
		resourceMap.put("ip", serverIp);
		
		return resourceMap;
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
