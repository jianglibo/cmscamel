package com.m3958.camel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.file.GenericFile;
import org.apache.commons.codec.binary.Hex;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.m3958.camel.util.Osdetecter;

public class YuiComboLog {

	public static class YuiCombo {
//		md5,args,num
		private Long id;
		
		private String md5;
		
		private String args;
		
		private Integer num;
		
		private boolean combined;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getMd5() {
			return md5;
		}

		public void setMd5(String md5) {
			this.md5 = md5;
		}

		public String getArgs() {
			return args;
		}

		public void setArgs(String args) {
			this.args = args;
		}

		public Integer getNum() {
			return num;
		}

		public void setNum(Integer num) {
			this.num = num;
		}

		public boolean isCombined() {
			return combined;
		}

		public void setCombined(boolean combined) {
			this.combined = combined;
		}
	}
	
	
	private String scriptSourceBase = "/data/staticyui";
	private String scriptCombinedSaveBase = "/data/staticyui/cache";
	
    private SimpleJdbcTemplate simpleJdbcTemplate;
    
    private SimpleJdbcInsert insertYuiComboLog;
    
    private static Pattern combineLogPtn = Pattern.compile(".*\"GET\\s+/combo\\?(.*)\\s+HTTP/.*");
    
    private static String FIND_BY_MD5_SQL = "select id, md5, args,num from argmd5 where md5 = ?";
    
    private static RowMapper<YuiCombo> YUI_COMBO_MAPPER = new RowMapper<YuiCombo>() {  
        public YuiCombo mapRow(ResultSet rs, int rowNum) throws SQLException {
        	YuiCombo yc = new YuiCombo();
            yc.setId(rs.getLong("id"));
            yc.setMd5(rs.getString("md5"));
            yc.setArgs(rs.getString("args"));
            yc.setNum(rs.getInt("num"));
            return yc;
        }
    };
    

	public YuiComboLog(DataSource ds){
		this.simpleJdbcTemplate = new SimpleJdbcTemplate(ds);
		this.insertYuiComboLog = new SimpleJdbcInsert(ds).
											withTableName("argmd5").
											usingColumns("md5", "args","num")
											.usingGeneratedKeyColumns("id");
		if(Osdetecter.isWindows()){
			this.scriptSourceBase = "E:/chdownload/yui_3.5.1/yui";
			this.scriptCombinedSaveBase = "E:/camelfiletest/to";
		}
	}
	
	public void p1(Exchange exchange) throws SQLException, IOException{
		Message in = exchange.getIn();
		GenericFile<File> gf = in.getBody(GenericFile.class);
		File f = new File(gf.getAbsoluteFilePath());
		
	    // notice the use of varargs since the parameter values now come 
	    // after the RowMapper parameter
		
		final Multiset<String> uniqueQuerys = HashMultiset.<String>create();
		
		Files.readLines(f, Charsets.UTF_8, new LineProcessor<String>() {
			@Override
			public boolean processLine(String line){
				
				Matcher m = combineLogPtn.matcher(line);
				if(m.matches()){
					String query = m.group(1);
					MessageDigest messageDigest;
					try {
						messageDigest = MessageDigest.getInstance("MD5");
						messageDigest.reset();
						messageDigest.update(query.getBytes(Charsets.UTF_8));
						byte[] resultByte = messageDigest.digest();
						String md5 = new String(Hex.encodeHex(resultByte));
						
						if(!uniqueQuerys.contains(md5)){//说明已经保存在数据库里面了。不需要再次查询
							YuiCombo yc = null;
							try {
								yc = simpleJdbcTemplate.queryForObject(YuiComboLog.FIND_BY_MD5_SQL, YuiComboLog.YUI_COMBO_MAPPER, md5);
							}catch (EmptyResultDataAccessException ee) {
								;//do nothing
							} 
							catch (DataAccessException e) {
								e.printStackTrace();
							}
							
							if(yc == null){//不存在，保存它
								yc = new YuiCombo();
								yc.setArgs(query);
								yc.setMd5(md5);
								yc.setNum(0);
								SqlParameterSource parameters = new BeanPropertySqlParameterSource(yc);
								Number newId = insertYuiComboLog.executeAndReturnKey(parameters);
							}
						}
						uniqueQuerys.add(md5);
					} catch (NoSuchAlgorithmException e) {
					}
				}
				return true;
			}

			@Override
			public String getResult() {
				return null;
			}
		});
		
		Set<Multiset.Entry<String>> aset = uniqueQuerys.entrySet();
		
		for(Multiset.Entry<String> entry : aset){
			String md5 = entry.getElement();
			int num = entry.getCount();
			YuiCombo yc = null;
			try {
				yc = simpleJdbcTemplate.queryForObject(YuiComboLog.FIND_BY_MD5_SQL, YuiComboLog.YUI_COMBO_MAPPER, md5);
			}catch (EmptyResultDataAccessException ee) {
				;//do nothing
			} 
			catch (DataAccessException e) {
				e.printStackTrace();
			}
			if(yc != null){
				try {
					this.simpleJdbcTemplate.update("update argmd5 set num = ? where md5 = ?", num + yc.getNum(),md5);
				} catch (DataAccessException e) {
					e.printStackTrace();
				}				
			}
		}
	}
	
	public void p2(Exchange exchange) throws SQLException, IOException{
		List<YuiCombo> combos = simpleJdbcTemplate.query("SELECT * FROM argmd5 WHERE combined = false AND num > ? ORDER BY num DESC LIMIT 0,50", YuiComboLog.YUI_COMBO_MAPPER,100);
		
		for(YuiCombo yc : combos){
			String[] ff = yc.getArgs().split("&");
			if(ff.length < 1)continue;
			String ext = ".js";
			boolean isjs = ff[0].endsWith(".js");
			if(!isjs)ext=".css";
			File tofile = new File(scriptCombinedSaveBase,yc.getMd5()+ ".cache" + ext);
			Files.createParentDirs(tofile);
			final BufferedWriter bw = Files.newWriter(tofile, Charsets.UTF_8);
			for(String f : ff){
				File inf = new File(scriptSourceBase,f);
				if(!inf.canRead())continue;
				Files.readLines(inf, Charsets.UTF_8, new LineProcessor<String>() {
					@Override
					public boolean processLine(String line) throws IOException {
						bw.write(line);
						bw.write("\n");
						return true;
					}
					@Override
					public String getResult() {
						return null;
					}
				});
			}
			bw.flush();
			bw.close();
			this.simpleJdbcTemplate.update("update argmd5 set combined = true where id = ?", yc.getId());
		}
	}
}
