package com.m3958.camel.unused;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.Message;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class ArticleSectionBean {
	
	private static ObjectMapper mapper = new ObjectMapper();
	
	private static Joiner SECTION_JOINER = Joiner.on(",").skipNulls();
	
	private static Splitter SECTION_SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();
	
	private static String ALONE_PATH = System.getProperty("user.dir");//"D:/workspace-sts-2.9.1.RELEASE/alone";
	
	private static File SECTION_PATH = new File(ALONE_PATH,"target/m3958/sections/");
	
	static{
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		if(!SECTION_PATH.exists()){
			SECTION_PATH.mkdirs();
		}
	}
	
	DataSource tcmsds;
	
	@Handler
	public void handler(Exchange exchange){
		
		Message in = exchange.getIn();
		
		 Map<String, Object> article = in.getBody(Map.class);
		
        Connection con = null;
        PreparedStatement pstmt = null;
        PreparedStatement pstmt1 = null;
        PreparedStatement pstmt2 = null;
        
        try{
            con = tcmsds.getConnection();
//            pstmt = con.prepareStatement("SELECT sections_id FROM article_section WHERE articles_id = ?");
//            pstmt1 = con.prepareStatement("select id,parentId,name from section_bases where id = ?");
//            pstmt2 = con.prepareStatement("select articles_id from article_section where sections_id = ?");
            
//  			String sids = fetchSections(pstmt,(String) article.get("id"));
//  			
//  			try {
//				for(Iterator<String> it=SECTION_SPLITTER.split(sids).iterator();it.hasNext();){
//					Map<String, Object> m;
//					m = writeSection(pstmt1, pstmt2,it.next());
//					for(int i = 0;i<10;i++){
//						if(m != null){
//							String pid = (String) m.get("parentId");
//							if( pid != null && !pid.isEmpty()){
//								m = writeSection(pstmt1,pstmt2, pid);
//							}
//						}
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//   			article.put("sectionids", sids);
    		in.setBody(mapper.writeValueAsString(article));
        } catch (Exception e) {
			e.printStackTrace();
		}finally{
        	if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException e) {}
        	if (pstmt1 != null)
				try {
					pstmt1.close();
				} catch (SQLException e) {}
        	if (pstmt2 != null)
				try {
					pstmt2.close();
				} catch (SQLException e) {}
        	if(con != null){
        		try {
					con.close();
				} catch (SQLException e) {}
        	}
        }

	}
	
	public Map<String, Object> writeSection(PreparedStatement pstmt1,PreparedStatement pstmt2,String sectionId) throws SQLException, JsonGenerationException, JsonMappingException, IOException{
		pstmt1.setString(1, sectionId);
		ResultSet rs = pstmt1.executeQuery();
		Map<String, Object> m = new HashMap<String, Object>();
		if(rs.next()){
			m.put("id", rs.getString("id"));
			m.put("parentId", rs.getString("parentId"));
			m.put("name",rs.getString("name"));
			
			pstmt2.setString(1, sectionId);
			ResultSet rs1 = pstmt2.executeQuery();
			List<String> articleids = new ArrayList<String>();
			while(rs1.next()){
				articleids.add(rs1.getString(1));
			}
			
			m.put("articleids", articleids);
			mapper.writeValue(new File(ALONE_PATH,"target/m3958/sections/" + m.get("id") + ".json"), m);
			return m;
		}
		
		return null;
	}
	
	
    public String fetchSections(PreparedStatement pstmt,String articleId) throws SQLException{
                pstmt.setString(1, articleId);
                ResultSet rs = pstmt.executeQuery();
                
                List<String> sectionids = new ArrayList<String>();
        	    while (rs.next()) {
        	        sectionids.add(rs.getString("sections_id"));
//        	        System.out.println(sectionids.size());
        	    }
        	    return SECTION_JOINER.join(sectionids);
        }

	public DataSource getTcmsds() {
		return tcmsds;
	}

	public void setTcmsds(DataSource tcmsds) {
		this.tcmsds = tcmsds;
	}
	
}
