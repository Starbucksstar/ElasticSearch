package com.star.es.msgconverter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.star.es.utils.IPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * 消息转换器
 * 自定义读取报文和返回报文前-->打印入参出参
 * @author gaoxing
 */
@Service
public class EsMessageConverter extends MappingJackson2HttpMessageConverter
{
	private static Logger logger = LoggerFactory.getLogger(EsMessageConverter.class);
	private List<MediaType> supportedMediaTypes = Arrays.asList(MediaType.APPLICATION_JSON);
	private ObjectMapper jsonMapper = new ObjectMapper();

	@Override
	public List<MediaType> getSupportedMediaTypes()
	{
		return supportedMediaTypes;
	}

	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType)
	{
		if (mediaType == null)
		{
			return true;
		}
		if (supportedMediaTypes.contains(mediaType))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotWritableException
	{
		byte[] b = StreamUtils.copyToByteArray(inputMessage.getBody());
		JSONObject jsonObject = JSONObject.parseObject(new String(b));
		logger.info("----------------[入参报文]------------------");
		logger.info(JSON.toJSONString(jsonObject));
		Class<?> targetClass = (type instanceof Class<?> ? (Class<?>) type : null);
		if (targetClass == null)
		{
			//获取type对应的可转换类型
			ResolvableType resolvableType = ResolvableType.forType(type);
			//Type对象转换为Class对象
			targetClass = resolvableType.resolve();
		}
		return jsonMapper.readValue(b, targetClass);
	}

	@Override
	protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException
	{
		JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(object));
		logger.info("----------------[出参报文]------------------");
		logger.info(JSON.toJSONString(jsonObject));
		super.writeInternal(object, type, outputMessage);
	}
}
