package com.mobigen.util.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mobigen.util.dao.SystemInfoDAO;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

@Service
public class SystemInfoService {

private static final Logger logger = LoggerFactory.getLogger(ResourceCollectService.class);
	
	@Autowired
	private SystemInfoDAO dao;
	
	@Value("${server.ip}")
	private String serverIp;
	
	public void getSystem() throws Exception {
		
		Map<String,String> resultMap = getSystemInfo();
		logger.info(resultMap.toString());
		
		List<Map<String,String>> systemList = dao.selectSystem(resultMap);
		
		// merge 기능 or insert & update 쿼리도 가능하지만..
		// 추 후 NVACCEL을 사용 할 수 있으므로 로직에서 처리한다.
		if(systemList == null || systemList.size() == 0) {
			// insert
			dao.insertSystem(resultMap);
		} else {
			// update
			dao.updateSystem(resultMap);
		}
	}
	
	private Map<String, String> getSystemInfo() {
		Map<String, String> systemInfoMap = new HashMap<String, String>();
		systemInfoMap.put("os", "");
		systemInfoMap.put("hostname", "");
		systemInfoMap.put("macAddress", "");
		systemInfoMap.put("cpu", "");
		systemInfoMap.put("memory", "");
		systemInfoMap.put("disk", "");
		systemInfoMap.put("ip", serverIp);
		
		SystemInfo si = new SystemInfo();

        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

        // os 
        systemInfoMap.put("os", os.toString());
        
        // hostname
        systemInfoMap.put("hostname", os.getNetworkParams().getHostName());
        
        // mac address
        systemInfoMap.put("macAddress", printMacAddress(hal.getNetworkIFs()));
        
        // cpu
        String cpu = hal.getProcessor().toString();
        int coreCnt = hal.getProcessor().getPhysicalProcessorCount();
        systemInfoMap.put("cpu", cpu + " " + coreCnt + "core");
        
        // memory
        systemInfoMap.put("memory", FormatUtil.formatBytes(hal.getMemory().getTotal()));
        
        // disk
        systemInfoMap.put("disk", printFileSystem(os.getFileSystem()));
		
		return systemInfoMap;
	}
	
	/**
	 * mac address
	 * @param networkIFs
	 * @return
	 */
	private String printMacAddress(NetworkIF[] networkIFs) {
		String macAddress = "";
        
		for (NetworkIF net : networkIFs) {
			// IPv4 주소가 있으며, ""이 아닐 경우의 mac address를 저장한다.
			if(net.getIPv4addr() != null && !net.getIPv4addr().equals("")) {
				macAddress = net.getMacaddr();
			}
        }
		return macAddress;
    }
	
	/**
	 * disk
	 * @param fileSystem
	 * @return
	 */
	private String printFileSystem(FileSystem fileSystem) {
		long totalSpace = 0L;
		
        OSFileStore[] fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray) {
            totalSpace += fs.getTotalSpace();
        }
        return FormatUtil.formatBytes(totalSpace);
    }
}
