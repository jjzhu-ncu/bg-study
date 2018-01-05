/**
 * @(#)App.java, 2017/9/29.
 * <p/>
 * Copyright 2017 ****, Inc. All rights reserved.
 * **** PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.jjzhu.calcite;

/**
 * @author 祝佳俊(zhujiajunup@163.com)
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
public class App {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        try {
            App app = new App();
            app.run();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Execution failed: " + e.getMessage(), e);
        }
    }

    private void run() {
        System.out.println(System.getProperty("user.dir")+"calcite\\target\\classes");
        try {
            Class.forName("org.apache.calcite.jdbc.Driver");
            Properties info = new Properties();
            info.setProperty("lex", "JAVA");
            try (final Connection connection = DriverManager.getConnection("jdbc:calcite:model="+System.getProperty("user.dir")+"\\calcite\\target\\classes\\model.json", info)) {
                int numberOfThreads = 1;
                ExecutorService threadPool = Executors.newFixedThreadPool(numberOfThreads);
                for (int i = 0; i < numberOfThreads; i++) {
                    final int threadId = i;
                    threadPool.submit(() -> {
                            DataFactory dataFactory = new DataFactory(threadId);
                            DescriptiveStatistics stats = new DescriptiveStatistics();
                            try (Statement statement = connection.createStatement()) {
                                for (int j = 0; j < 100; j++) {
                                    String query = createQuery(dataFactory);
                                    LOGGER.log(Level.INFO, "[" + threadId + "] Executing query " + query + " rows.");
                                    long startMillis = System.currentTimeMillis();
                                    try (ResultSet resultSet = statement.executeQuery(query)) {
                                        int count = 0;
                                        while (resultSet.next()) {
                                            count = resultSet.getInt(1);
                                        }
                                        long executionTime = System.currentTimeMillis() - startMillis;
                                        if (j > 3) {
                                            stats.addValue(executionTime);
                                        }
                                        LOGGER.log(Level.INFO, "[" + threadId + "] Result has " + count + " rows: " + executionTime);
                                    } catch (Exception e) {
                                        LOGGER.log(Level.SEVERE, "Query failed: " + e.getMessage(), e);
                                    }
                                }
                            } catch (Exception e) {
                                LOGGER.log(Level.SEVERE, "Query failed: " + e.getMessage(), e);
                            }
                            LOGGER.log(Level.INFO, "[" + threadId + "] Avg. execution time: " + DecimalFormat.getInstance().format(stats.getMean()) + "ms");
                        }
                    );
                }
                threadPool.shutdown();
                threadPool.awaitTermination(1, TimeUnit.HOURS);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    private String createQuery(DataFactory dataFactory) {
        return "select count(*) from persons p inner join addresses a on a.personId = p.id where a.city = '" + dataFactory.getNextCity() + "' and p.lastName = '" + dataFactory.getNextLastName() + "'";
        //return "select count(*) from persons p where p.lastName = '" + dataFactory.getNextLastName() + "'";
    }
}