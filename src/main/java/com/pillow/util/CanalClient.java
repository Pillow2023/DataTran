package com.pillow.util;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry.*;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Slf4j
public class CanalClient {

    @Value("${canal.instance.host:}")
    private String ip;

    @Value("${canal.instance.port:}")
    private Integer port;

    @Value("${canal.mq.topic:}")
    private String destination;

    @Value("${canal.deal.batchSize:}")
    private Integer batchSize;

    @Value("${canal.file.ftpPath:}")
    private String ftpPath;


    private static String sqlSendPath;


    private static String sqlReplyPath;


    private static String sqlCompletePath;

    private static String SQL_FILE_SUFFIX = ".sql";

    @Value("${canal.file.rootPath:}")
    public void setFilePath(String rootPath){
        String root = FileUtil.dirpathFormat(rootPath,true);
        sqlSendPath = FileUtil.dirPathSplice(root,"send");
        sqlReplyPath = FileUtil.dirPathSplice(root,"reply");
        sqlCompletePath = FileUtil.dirPathSplice(root,"complete");
    }


    //sql队列
    private Queue<String> SQL_QUEUE = new ConcurrentLinkedQueue<>();

    /**
     * canal入库方法
     */
    public void run() {
        //连接canal
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(ip,
                port), destination, "", "");
        try {
            connector.connect();
            connector.subscribe(".*\\..*");
            connector.rollback();
            try {
                while (true) {
                    //尝试从master那边拉去数据batchSize条记录，有多少取多少
                    Message message = connector.getWithoutAck(batchSize);
                    long batchId = message.getId();
                    int size = message.getEntries().size();
                    //无数据处理，睡眠1秒
                    if (batchId == -1 || size == 0) {
                        Thread.sleep(1000);
                    } else {
                        dataHandle(message.getEntries());
                    }
                    connector.ack(batchId);

                    //当队列里面堆积的sql大于一定数值的时候就模拟执行
                    if (SQL_QUEUE.size() >= 1) {
                        saveQueueSqlToFile();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            connector.disconnect();
        }
    }

    /**
     * @author: Pillow2023
     * @date: 2023/3/1
     * @Title: saveQueueSqlToFile
     * @Description : 将队列中的sql语句逐条导出为sql文件
     * @param
     * @return void
     */
    public void saveQueueSqlToFile() throws IOException {
        for(int i = 0;i< SQL_QUEUE.size();i++){
            String sql = SQL_QUEUE.poll();
            log.info("[sql]---->"+sql);
            String fileName = DateFormatUtil.getLocalDateTime("yyyy-MM-dd HH-mm-ss-SSS")+SQL_FILE_SUFFIX;
            File file = new File(FileUtil.getFilePath(sqlSendPath,fileName));
            FileOutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(file);
                outputStream.write(sql.getBytes());
            } catch (Exception e) {
                log.error("sql:'"+sql+"'"+"写入"+file.getName()+"失败");
            } finally {
                if(outputStream!=null){
                    outputStream.close();
                }
            }
            //上传至目标服务器ftp目录
            FtpUtil.upload(ftpPath,file);
        }
    }


    /**
     * @author: Pillow2023
     * @date: 2023/3/1
     * @Title: dataHandle
     * @Description : 数据处理
     * @param entrys
     * @return void
     */
    private void dataHandle(List<Entry> entrys) throws InvalidProtocolBufferException {
        for (Entry entry : entrys) {
            if (EntryType.ROWDATA == entry.getEntryType()) {
                RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
                //获取事件类型
                EventType eventType = rowChange.getEventType();
                if(EventType.CREATE.equals(eventType)||EventType.ERASE.equals(eventType)||EventType.ALTER.equals(eventType)){//创建
                    saveDDLSql(entry);
                }else if(EventType.INSERT.equals(eventType)){
                    saveInsertSql(entry);
                }else if(EventType.UPDATE.equals(eventType)){
                    saveUpdateSql(entry);
                }else if(EventType.DELETE.equals(eventType)){
                    saveDeleteSql(entry);
                }
            }
        }
    }




    /**
     * @author: Pillow2023
     * @date: 2023/3/1
     * @Title: saveDDLSql
     * @Description : 保存DDL语句
     * @param entry
     * @return void
     */
    private void saveDDLSql(Entry entry){
        try {
            RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
            String sql = rowChange.getSql();
            SQL_QUEUE.add(sql);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: Pillow2023
     * @date: 2023/3/1
     * @Title: saveInsertSql
     * @Description : 保存insert语句，无法
     * @param entry
     * @return void
     */
    private void saveInsertSql(Entry entry) {
        try {
            RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
            List<RowData> rowDataList = rowChange.getRowDatasList();
            for(RowData rowData:rowDataList){
                List<Column> columnList = rowData.getAfterColumnsList();
                StringBuffer sql = new StringBuffer("INSERT INTO "+entry.getHeader().getTableName()+" (");
                for(int i=0;i<columnList.size();i++){
                    sql.append(columnList.get(i).getName());
                    if(i != columnList.size()-1){
                        sql.append(",");
                    }
                }
                sql.append(") VALUES (");
                for(int i=0;i<columnList.size();i++){
                    sql.append("'"+columnList.get(i).getValue()+"'");
                    if(i != columnList.size()-1){
                        sql.append(",");
                    }
                }
                sql.append(")");
                SQL_QUEUE.add(sql.toString());
            }

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }


    /**
     * @author: Pillow2023
     * @date: 2023/3/1
     * @Title: saveUpdateSql
     * @Description : 保存sql文件
     * @param entry
     * @return void
     */
    private void saveUpdateSql(Entry entry) {
        try {
            RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
            List<RowData> rowDataList = rowChange.getRowDatasList();
            for (RowData rowData : rowDataList) {
                List<Column> newColumnList = rowData.getAfterColumnsList();
                StringBuffer sql = new StringBuffer("UPDATE "+entry.getHeader().getTableName()+" SET");
                for(int i = 0;i<newColumnList.size();i++){
                    sql.append(" "+ newColumnList.get(i).getName()+"= '"+newColumnList.get(i).getValue()+"'");
                    if(i != newColumnList.size()-1){
                        sql.append(",");
                    }
                }
                sql.append(" WHERE ");
                List<Column> oldColumnList = rowData.getBeforeColumnsList();
                for(Column column:oldColumnList){
                    if(column.getIsKey()){
                        sql.append(column.getName() + "=" +column.getValue());
                        break;
                    }
                }
                SQL_QUEUE.add(sql.toString());
            }
        }catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    /**
     * @author: Pillow2023
     * @date: 2023/3/1
     * @Title: saveDeleteSql
     * @Description : 保存删除sql
     * @param entry
     * @return void
     */
    private void saveDeleteSql(Entry entry) {
        try {
            RowChange rowChange = RowChange.parseFrom(entry.getStoreValue());
            List<RowData> rowDataList = rowChange.getRowDatasList();
            for (RowData rowData : rowDataList) {
                List<Column> columnList = rowData.getBeforeColumnsList();
                StringBuffer sql = new StringBuffer("DELETE FROM "+entry.getHeader().getTableName()+" WHERE ");
                for(Column column:columnList){
                    if(column.getIsKey()){
                        sql.append(column.getName()+ "="+column.getValue());
                        break;
                    }
                }
                SQL_QUEUE.add(sql.toString());
            }
        }catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }


}

