package com.star.es.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPUtil
{
	private static Logger logger = LoggerFactory.getLogger(IPUtil.class);
	private static final String LOCALIPADDR = "127.0.0.1";
	private static final String UNKNOW = "unknow";

	public static String getIpAddress(HttpServletRequest request)
	{
		String ip = request.getHeader("x-forwarded-for");
		if (StringUtils.isBlank(ip) || UNKNOW.equalsIgnoreCase(ip))
		{
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (StringUtils.isBlank(ip) || UNKNOW.equalsIgnoreCase(ip))
		{
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (StringUtils.isBlank(ip) || UNKNOW.equalsIgnoreCase(ip))
		{
			ip = request.getRemoteAddr();
			if (ip.equals(LOCALIPADDR))
			{
				InetAddress inetAddress = null;
				try
				{
					inetAddress = InetAddress.getLocalHost();
				}
				catch (UnknownHostException e)
				{
					logger.error("获取本机ip地址异常,异常信息 ={}", e.getMessage(), e);
				}
				ip = inetAddress.getHostAddress();
			}
		}
		if (StringUtils.isNotBlank(ip) && StringUtils.length(ip) > 15)
		{
			if (ip.indexOf(",") > 0)
			{
				ip = ip.substring(0, ip.indexOf(","));
			}
		}
		return ip;
	}

}
