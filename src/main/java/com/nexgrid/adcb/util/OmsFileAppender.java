package com.nexgrid.adcb.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.ibatis.io.Resources;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;


@Plugin(name = "OmsFileAppender", category = "Core", elementType = "appender", printObject = true)
public class OmsFileAppender extends AbstractAppender{
	// 로그를 위한 상수값
	static final int TOP_OF_TROUBLE = -1;
	static final int TOP_OF_MINUTE = 0;
	static final int TOP_OF_HOUR = 1;
	static final int HALF_DAY = 2;
	static final int TOP_OF_DAY = 3;
	static final int TOP_OF_WEEK = 4;
	static final int TOP_OF_MONTH = 5;
	static final TimeZone GMTTIMEZONE = TimeZone.getTimeZone("GMT");

	// 폴더 설정용
	public static final String DEFAULT_DIRECTORY = "logs";
	public static final String DEFAULT_SUFFIX = ".log";
	public static final String DEFAULT_PREFIX = "svc";

	private String directory = DEFAULT_DIRECTORY;
	private String prefix = DEFAULT_PREFIX;
	private String suffix = DEFAULT_SUFFIX;

	private BruceRollingCalendar rolling = null;

	// 파일 갱신을 위한 시간
	private String path;
	private long nextCheck = System.currentTimeMillis() - 1;
	private String scheduledFilename;
	private int rollingTerms;
	
	// 파일 참고용 날자형식	
	private String datePattern = "'.'yyyyMMddHHmm";
	private String folderPattern = "yyyyMMdd";
	private Date now;
	private SimpleDateFormat sDateFormat;

	
	protected OmsFileAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Map appenderInfo) {
		super(name, filter, layout, false);
		setDirectory(appenderInfo.get("directory").toString());
		setPrefix(appenderInfo.get("prefix").toString());
		setRollingTerms(Integer.parseInt(appenderInfo.get("rollingTerms").toString()));
		setDatePattern(appenderInfo.get("datePattern").toString());
		setFolderPattern(appenderInfo.get("folderPattern").toString());
		activateOptions(name);
	}
	
	// 로그파일을 준비한다.
	public void activateOptions(String name) {
		if (datePattern != null && prefix != null) {
			try {
				now = new Date(System.currentTimeMillis());
				sDateFormat = new SimpleDateFormat(datePattern);

				// 파일 갱신 주기 설정
				rolling = new BruceRollingCalendar();
				if (rollingTerms > 0) {
					rolling.setRollingTerms(rollingTerms);
				}
				
				int type = computeCheckPeriod();
				rolling.setType(type);
				
				// 디렉토리 생성
				File dir = new File(directory);
				if (dir.isAbsolute()) {
					dir.mkdirs();
				}

				// 날짜폴더 생성
				path = directory + new SimpleDateFormat(folderPattern).format(now) + "/";

				File nFile = new File(path);
				if (nFile.isAbsolute()) {
					nFile.mkdirs();
				}

				if (nFile.canWrite()) {
					if (Init.readConfig.getServer_num() == null ||  Init.readConfig.getServer_num() == "")
					{
						 Properties props = new Properties();
						 
						InputStream isU = Resources.getResourceAsStream("/conf/properties/config_properties.xml");
						
						props.loadFromXML(isU);
						
						Init.readConfig.setServer_num(props.getProperty("SERVER_NUM"));
					}
					
					scheduledFilename = path + prefix + "." + Init.readConfig.getServer_num() + sDateFormat.format(rolling.getCurrentCheck(now)) + suffix;
					
					//this.setFile(scheduledFilename, true, this.bufferedIO, this.bufferSize);
					createFile(scheduledFilename);
				} else {
					getHandler().error("Cannot write for appender [" + name + "] : " + path);
					return;
				}
			} catch (IOException e) {
				getHandler().error("setFile(" + scheduledFilename + ", true) call failed.");
			}
		}

	
	}
	
	
	int computeCheckPeriod() {
		BruceRollingCalendar rollingCalendar = new BruceRollingCalendar(GMTTIMEZONE, Locale.getDefault());
		// set sate to 1970-01-01 00:00:00 GMT
		Date epoch = new Date(0);
		if (datePattern != null) {
			for (int i = TOP_OF_MINUTE; i <= TOP_OF_MONTH; i++) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
				simpleDateFormat.setTimeZone(GMTTIMEZONE); // do all date
															// formatting in GMT
				String r0 = simpleDateFormat.format(epoch);
				rollingCalendar.setType(i);
				Date next = new Date(rollingCalendar.getNextCheckMillis(epoch));
				String r1 = simpleDateFormat.format(next);
				// // System.out.println("Type = "+i+", r0 = "+r0+", r1 = "+r1);
				if (r0 != null && r1 != null && !r0.equals(r1)) {
					return i;
				}
			}
		}
		return TOP_OF_TROUBLE; 
	}


	@Override
	public void append(LogEvent event) {
		// TODO Auto-generated method stub
		
		long cTime = System.currentTimeMillis();
		if (cTime >= nextCheck) {
			now.setTime(cTime);
			nextCheck = rolling.getNextCheckMillis(now);
			try {
				rollOver();
			} catch (IOException ioe) {
				if (ioe instanceof InterruptedIOException) {
					Thread.currentThread().interrupt();
				}
				//LogLog.error("rollOver() failed.", ioe);
			}
		}
		if("".equals(event.getMessage().toString())) {
			return;
		}
		
		final byte[] bytes = getLayout().toByteArray(event);
		writerFile(bytes);
	}
	
	
	// 로그파일을 주기에 맞추어 갱신한다.
		void rollOver() throws IOException {
			if (datePattern == null) {
				getHandler().error("Missing DatePattern option in rollOver().");
				return;
			}

			dailyFolder(now);
			
//			if ( Init.readConfig.getServer_num() == null ||  Init.readConfig.getServer_num() == "")
//				return;
			
			// 현재 파일명 찾기
			String nextFilename = path + prefix + "." + Init.readConfig.getServer_num() + sDateFormat.format(rolling.getCurrentCheck(now)) + suffix;
				
			// 기존 파일과 같으면 리턴
			if (scheduledFilename.equals(nextFilename)) {
				return;
			}
			
			// 현재 파일 종료
			//this.closeFile();
			
			try {
				// This will also close the file. This is OK since multiple
				// close operations are safe.
				//this.setFile(nextFilename, true, this.bufferedIO, this.bufferSize);
				createFile(nextFilename);
			} catch (Exception e) {
				getHandler().error("setFile(" + nextFilename + ", true) call failed.");
			}
			scheduledFilename = nextFilename;

		}
		
		private void dailyFolder(Date now) {
			String nPath = null;

			nPath = directory + new SimpleDateFormat("yyyyMMdd").format(now);

			File nFile = new File(nPath);

			if (nFile.isAbsolute()) {
				nFile.mkdirs();

				this.path = nFile.getPath() + "/";
			}
		}
		
	
	
	@PluginFactory
	public static OmsFileAppender createAppender(@PluginAttribute("name") String name, 
		      @PluginElement("Filter") Filter filter, @PluginElement("Layout") Layout<? extends Serializable> layout,
		      @PluginAttribute("ignoreExceptions") boolean ignoreExceptions, @PluginAttribute("directory") String directory,
		      @PluginAttribute("prefix")String prefix, @PluginAttribute("rollingTerms")int rollingTerms,
		      @PluginAttribute("datePattern")String datePattern, @PluginAttribute("folderPattern")String folderPattern) {
		
		
		/*if (!createFile(fileName)) {
            return null;
        }*/
		
		if (name == null) {
            LOGGER.error("no name defined in conf.");
            return null;
        }
        if (layout == null) {
        	layout = PatternLayout.createDefaultLayout();
        }
		Map appenderInfo = new HashMap<String, String>();
		appenderInfo.put("directory", directory);
		appenderInfo.put("prefix", prefix);
		appenderInfo.put("rollingTerms", rollingTerms);
		appenderInfo.put("datePattern", datePattern);
		appenderInfo.put("folderPattern", folderPattern);
		
		return new OmsFileAppender(name, filter, layout, ignoreExceptions, appenderInfo);
	}
	
	
	private static boolean createFile(String fileName) {
        Path filePath = Paths.get(fileName);
        try {

            /*if (Files.exists(filePath)) {
                Files.delete(filePath);
            }Files.createFile(filePath);*/
        	if (!Files.exists(filePath)) {
        		Files.createFile(filePath);
            }
            
        } catch (IOException e) {
            LOGGER.error("create file exception", e);
            return false;
        }
        return true;
    }

	
	
	 private void writerFile(byte[] log) {
	        try {
	            Files.write(Paths.get(scheduledFilename), log, StandardOpenOption.APPEND);
	        } catch (IOException e) {
	            LOGGER.error("write file exception", e);
	        }
	 }
	 
	 
		public String getDirectory() {
			return directory;
		}

		public void setDirectory(String directory) {
			if (directory == null || directory.length() == 0) {
				// Set to here
				this.directory = ".";
			} else {
				
				if(System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows")) {
					directory = "C:/" + directory;
				}
				
				if (directory.endsWith("/")) {
					this.directory = directory;
				} else {
					this.directory = directory + "/";
				}
			}
		}

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			if (prefix == null) {
				// Set to default
				this.prefix = DEFAULT_PREFIX;
			} else {
				this.prefix = prefix;
			}
		}

		public String getSuffix() {
			return suffix;
		}

		public void setSuffix(String suffix) {
			if (suffix == null) {
				// Set to empty, not default
				this.suffix = "";
			} else {
				this.suffix = suffix;
			}
		}

		public void setFolderPattern(String folderPattern) {
			if (folderPattern != null) {
				this.folderPattern = folderPattern;
			}
		}

		public void setDatePattern(String datePattern) {
			if (datePattern != null) {
				this.datePattern = datePattern;
			}
		}

		public void setRollingTerms(int rollingTerms) {
			this.rollingTerms = rollingTerms;
		}

		
	}

	/**
	 * RollingCalendar is a helper class to DailyOmsFileAppender. Given a periodicity type and the
	 * current time, it computes the start of the next interval.
	 */
	class BruceRollingCalendar extends GregorianCalendar {

		/**
		 * 분 단위로 파일 백업....
		 */
		private static final int ROLLING_TERM_MINUTE = 10;

		private static final long serialVersionUID = -3560331770601814177L;

		int type = OmsFileAppender.TOP_OF_TROUBLE;

		int rollingTerms = ROLLING_TERM_MINUTE;

		BruceRollingCalendar() {
			super();
		}

		BruceRollingCalendar(TimeZone tz, Locale locale) {
			super(tz, locale);
		}

		void setType(int type) {
			this.type = type;
		}

		public void setRollingTerms(int rollingTerms) {
			this.rollingTerms = rollingTerms;
		}

		public long getNextCheckMillis(Date now) {
			return getNextCheckDate(now).getTime();
		}
		
		public Date getNextCheckDate(Date now) {
			this.setTime(now);

			switch (type) {
				case OmsFileAppender.TOP_OF_MINUTE:
					
					/*
					 * 매 시 정각을 기준으로 어떻게 인터벌을 계산하여 줄 것 인가?
					 * 
					 * 00, 05, 10 , 15, 20, 25, 30, 35, 40, 45, 50, 55
					 */
			
					int curtMinute = this.get(MINUTE);
					
					int nextMTime = 0;
					
					do {
						nextMTime += rollingTerms;
					} while (nextMTime < curtMinute);
					
					if (nextMTime >= 60) {
						// 다음 정각 시간
						this.set(Calendar.SECOND, 0);
						this.set(Calendar.MILLISECOND, 0);
						this.set(Calendar.MINUTE, 0);
						this.add(Calendar.HOUR, 1);
						
					} else {
						this.set(Calendar.SECOND, 0);
						this.set(Calendar.MILLISECOND, 0);
						this.set(Calendar.MINUTE, nextMTime);
					}
					
					break;
				case OmsFileAppender.TOP_OF_HOUR:
					this.set(Calendar.MINUTE, 0);
					this.set(Calendar.SECOND, 0);
					this.set(Calendar.MILLISECOND, 0);
					this.add(Calendar.HOUR_OF_DAY, 1);
					break;
				case OmsFileAppender.HALF_DAY:
					this.set(Calendar.MINUTE, 0);
					this.set(Calendar.SECOND, 0);
					this.set(Calendar.MILLISECOND, 0);
					int hour = get(Calendar.HOUR_OF_DAY);
					if (hour < 12) {
						this.set(Calendar.HOUR_OF_DAY, 12);
					} else {
						this.set(Calendar.HOUR_OF_DAY, 0);
						this.add(Calendar.DAY_OF_MONTH, 1);
					}
					break;
				case OmsFileAppender.TOP_OF_DAY:
					this.set(Calendar.HOUR_OF_DAY, 0);
					this.set(Calendar.MINUTE, 0);
					this.set(Calendar.SECOND, 0);
					this.set(Calendar.MILLISECOND, 0);
					this.add(Calendar.DATE, 1);
					break;
				case OmsFileAppender.TOP_OF_WEEK:
					this.set(Calendar.DAY_OF_WEEK, getFirstDayOfWeek());
					this.set(Calendar.HOUR_OF_DAY, 0);
					this.set(Calendar.MINUTE, 0);
					this.set(Calendar.SECOND, 0);
					this.set(Calendar.MILLISECOND, 0);
					this.add(Calendar.WEEK_OF_YEAR, 1);
					break;
				case OmsFileAppender.TOP_OF_MONTH:
					this.set(Calendar.DATE, 1);
					this.set(Calendar.HOUR_OF_DAY, 0);
					this.set(Calendar.MINUTE, 0);
					this.set(Calendar.SECOND, 0);
					this.set(Calendar.MILLISECOND, 0);
					this.add(Calendar.MONTH, 1);
					break;
				default:
					throw new IllegalStateException("Unknown periodicity type.");
			}
			return getTime();
		}
		
		public Date getCurrentCheck(Date now) {
			this.setTime(now);
			
			switch (type) {
				case OmsFileAppender.TOP_OF_MINUTE:
					
					/*
					 * 매 시 정각을 기준으로 어떻게 인터벌을 계산하여 줄 것 인가?
					 * 
					 * 00, 05, 10 , 15, 20, 25, 30, 35, 40, 45, 50, 55
					 */
			
					int curtMinute = this.get(MINUTE);
					
					if (this.get(MINUTE) - rollingTerms < 0) {
						// 정각 시간
						this.set(Calendar.SECOND, 0);
						this.set(Calendar.MILLISECOND, 0);
						this.set(Calendar.MINUTE, 0);
					
					} else {
						
						int nextMTime = 0;
						
						do {
							nextMTime += rollingTerms;
						} while (nextMTime <= curtMinute);
						
						nextMTime -= rollingTerms;
						
						this.set(Calendar.SECOND, 0);
						this.set(Calendar.MILLISECOND, 0);
						this.set(Calendar.MINUTE, nextMTime);
					}
					
					break;
				case OmsFileAppender.TOP_OF_HOUR:
					this.set(Calendar.MINUTE, 0);
					this.set(Calendar.SECOND, 0);
					this.set(Calendar.MILLISECOND, 0);
					this.add(Calendar.HOUR_OF_DAY, 1);
					break;
				case OmsFileAppender.HALF_DAY:
					this.set(Calendar.MINUTE, 0);
					this.set(Calendar.SECOND, 0);
					this.set(Calendar.MILLISECOND, 0);
					int hour = get(Calendar.HOUR_OF_DAY);
					if (hour < 12) {
						this.set(Calendar.HOUR_OF_DAY, 12);
					} else {
						this.set(Calendar.HOUR_OF_DAY, 0);
						this.add(Calendar.DAY_OF_MONTH, 1);
					}
					break;
				case OmsFileAppender.TOP_OF_DAY:
					this.set(Calendar.HOUR_OF_DAY, 0);
					this.set(Calendar.MINUTE, 0);
					this.set(Calendar.SECOND, 0);
					this.set(Calendar.MILLISECOND, 0);
					this.add(Calendar.DATE, 1);
					break;
				case OmsFileAppender.TOP_OF_WEEK:
					this.set(Calendar.DAY_OF_WEEK, getFirstDayOfWeek());
					this.set(Calendar.HOUR_OF_DAY, 0);
					this.set(Calendar.MINUTE, 0);
					this.set(Calendar.SECOND, 0);
					this.set(Calendar.MILLISECOND, 0);
					this.add(Calendar.WEEK_OF_YEAR, 1);
					break;
				case OmsFileAppender.TOP_OF_MONTH:
					this.set(Calendar.DATE, 1);
					this.set(Calendar.HOUR_OF_DAY, 0);
					this.set(Calendar.MINUTE, 0);
					this.set(Calendar.SECOND, 0);
					this.set(Calendar.MILLISECOND, 0);
					this.add(Calendar.MONTH, 1);
					break;
				default:
					throw new IllegalStateException("Unknown periodicity type.");
			}
			return getTime();
		}
	
	
	


	
}
