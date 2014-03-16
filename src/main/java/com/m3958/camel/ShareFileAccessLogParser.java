package com.m3958.camel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.shiro.crypto.AesCipherService;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;


import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.io.LineProcessor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;
import com.m3958.camel.util.Osdetecter;

public class ShareFileAccessLogParser {
	
	public static class CombinedLog{
		@Expose
		private String remoteIp;
		@Expose
		private Date accessTime;
		@Expose
		private String request;
		@Expose
		private int response;
		@Expose
		private long byteSent;
		@Expose
		private String refererHost;
		@Expose
		private String refererPath;
		@Expose
		private String browser;
		@Expose
		private long assetId;
		

		
		public String getRefererHost() {
			return refererHost;
		}
		public void setRefererHost(String refererHost) {
			this.refererHost = refererHost;
		}
		public String getRefererPath() {
			return refererPath;
		}
		public void setRefererPath(String refererPath) {
			this.refererPath = refererPath;
		}
		
		public String getRemoteIp() {
			return remoteIp;
		}
		public void setRemoteIp(String remoteIp) {
			this.remoteIp = remoteIp;
		}
		public Date getAccessTime() {
			return accessTime;
		}
		public void setAccessTime(Date accessTime) {
			this.accessTime = accessTime;
		}
		public String getRequest() {
			return request;
		}
		public void setRequest(String request) {
			this.request = request;
		}
		public int getResponse() {
			return response;
		}
		public void setResponse(int response) {
			this.response = response;
		}
		public long getByteSent() {
			return byteSent;
		}
		public void setByteSent(long byteSent) {
			this.byteSent = byteSent;
		}

		public String getBrowser() {
			return browser;
		}
		public void setBrowser(String browser) {
			this.browser = browser;
		}
		public long getAssetId() {
			return assetId;
		}
		public void setAssetId(long assetId) {
			this.assetId = assetId;
		}
	}
	
	
	private static Pattern LOG_ENTRY_PATTERN = Pattern.compile("^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+) \"([^\"]+)\" \"([^\"]+)\"");
	private static Pattern GASSET_LOG_ENTRY_PATTERN = Pattern.compile("GET /gasset/(\\d+).*");
	
	private static Pattern REFERER_LOG_ENTRY_PATTERN = Pattern.compile("https?://([^/]+)(.*)");
	
//	31/Aug/2012:15:17:02 +0800
	private static DateFormat formatter = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z",Locale.US);
	
	private static Gson gson = new GsonBuilder()
											    .enableComplexMapKeySerialization()
											    .excludeFieldsWithoutExposeAnnotation()
											    .serializeNulls()
											    .registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
													  @Override
													  public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext
													             context) {
													    return src == null ? null : new JsonPrimitive(src.getTime());
													  }
													})
											    .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
													  @Override
													  public Date deserialize(JsonElement json, Type typeOfT,
													       JsonDeserializationContext context) throws JsonParseException {
													    return json == null ? null : new Date(json.getAsLong());
													  }
												})
											    .create();
	
	
    private JdbcTemplate  jdbcTemplate;
    
    private String jsonSaveTo = "/opt/camelworld/fromdir/accesslogjson";
	
	public ShareFileAccessLogParser(DataSource ds){
		this.jdbcTemplate = new JdbcTemplate(ds);
		if(Osdetecter.isWindows()){
			jsonSaveTo = "e:/camelfiletest/to";
		}
	}
	
	
	private Long currentId = null;
	
	private Long topId = null;
	
	private AesCipherService aceCipherService;
	
	private synchronized Long nextId(){
		Long nextId;
		if(currentId == null){
			Long lbefore = jdbcTemplate.queryForLong("select APP_SEQ_VALUE from APP_SEQ_STORE where APP_SEQ_NAME = ?","APP_SHAREFILE.APP_GASSET_ACCESS_PK");
			jdbcTemplate.update("update APP_SEQ_STORE set APP_SEQ_VALUE = APP_SEQ_VALUE + 100 where APP_SEQ_NAME = ?","APP_SHAREFILE.APP_GASSET_ACCESS_PK");
			Long lafter = jdbcTemplate.queryForLong("select APP_SEQ_VALUE from APP_SEQ_STORE where APP_SEQ_NAME = ?","APP_SHAREFILE.APP_GASSET_ACCESS_PK");
			currentId = lbefore;
			topId = lafter;
		}
		
		nextId = currentId;
		currentId++;
		if(currentId >= topId){
			currentId = null;
			topId = null;
		}
		return nextId;
	}
	
	public void p1(Exchange exchange) throws SQLException, IOException{
		Message in = exchange.getIn();
		final Reader reader = new InputStreamReader(in.getBody(InputStream.class),Charsets.UTF_8);
		
		final List<CombinedLog> aggregated = Lists.newArrayList();
		
		CharStreams.readLines(new InputSupplier<Reader>() {
			@Override
			public Reader getInput() throws IOException {
				return reader;
			}
		}, new LineProcessor<String>() {
			@Override
			public boolean processLine(String line) throws IOException {
				Matcher matcher = LOG_ENTRY_PATTERN.matcher(line);
				if(matcher.matches()){
					String request = matcher.group(5);
					Matcher requestMatcher = GASSET_LOG_ENTRY_PATTERN.matcher(request);
					if(!requestMatcher.matches()){
						return true;
					}
					CombinedLog cl = new CombinedLog();
					cl.setAssetId(Long.parseLong(requestMatcher.group(1)));
					cl.setRemoteIp(matcher.group(1));
					String accessTimeStr = matcher.group(4);
					try {
						cl.setAccessTime((Date) formatter.parse(accessTimeStr));
					} catch (ParseException e) {
						e.printStackTrace();
					}
					cl.setRequest(request);
					cl.setResponse(Integer.parseInt(matcher.group(6)));
					cl.setByteSent(Long.parseLong(matcher.group(7)));
					
					String refererStr = matcher.group(8);

					Matcher refererMatcher = REFERER_LOG_ENTRY_PATTERN.matcher(refererStr);
					if(refererMatcher.matches()){
						cl.setRefererHost(refererMatcher.group(1));
						cl.setRefererPath(refererMatcher.group(2));
					}else{
						cl.setRefererHost(null);
						cl.setRefererPath(null);
					}
					cl.setBrowser(matcher.group(9));
					aggregated.add(cl);
					System.out.println(cl);
					
					
//					APP_SHAREFILE.APP_GASSET_ACCESS_PK
					
//					| APP_SEQ_STORE | CREATE TABLE `app_seq_store` (
//							  `APP_SEQ_NAME` varchar(50) NOT NULL,
//							  `APP_SEQ_VALUE` decimal(38,0) default NULL,
//							  PRIMARY KEY  (`APP_SEQ_NAME`)
//							) ENGINE=InnoDB DEFAULT CHARSET=utf8 |
							

//					| GASSET_ACCESS | CREATE TABLE `gasset_access` (
//							  `id` bigint(20) NOT NULL,
//							  `ACCESSTIME` datetime default NULL,
//							  `REFERERHOST` varchar(255) default NULL,
//							  `REFERERPATH` varchar(255) default NULL,
//							  `TRANSFERSIZE` bigint(20) default NULL,
//							  `gasset_id` bigint(20) default NULL,
//							  PRIMARY KEY  (`id`),
//							  KEY `FK_G_ASSET_ACCESS_gasset_id` (`gasset_id`),
//							  CONSTRAINT `FK_G_ASSET_ACCESS_gasset_id` FOREIGN KEY (`gasset_id`) REFERENCES
//							`g_asset` (`id`)
//							) ENGINE=InnoDB DEFAULT CHARSET=utf8 |
					
					if(aggregated.size() == 100){
						String s = gson.toJson(aggregated);
						String uuid = UUID.randomUUID().toString();
						Files.write(s, new File(jsonSaveTo,uuid), Charsets.UTF_8);
						Files.touch(new File(jsonSaveTo,uuid + ".done"));
						aggregated.clear();
					}
				}
				return true;
			}

			@Override
			public String getResult() {
				return null;
			}
		});
		
		if(aggregated.size() > 0){
			String s = gson.toJson(aggregated);
			String uuid = UUID.randomUUID().toString();
			Files.write(s, new File(jsonSaveTo,uuid), Charsets.UTF_8);
			Files.touch(new File(jsonSaveTo,uuid + ".done"));
		}
	}
	
	private int[] batchUpdate(final List<CombinedLog> logs){
		int[] updateCounts = jdbcTemplate.batchUpdate("insert into GASSET_ACCESS values (?,?,?,?,?)", new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int index) throws SQLException {
//                ps.setString(1, actors.get(i).getFirstName());
//                ps.setString(2, actors.get(i).getLastName());
//                ps.setLong(3, actors.get(i).getId().longValue());
			}
			@Override
			public int getBatchSize() {
				return logs.size();
			}
		});
		return updateCounts;
	}
}
