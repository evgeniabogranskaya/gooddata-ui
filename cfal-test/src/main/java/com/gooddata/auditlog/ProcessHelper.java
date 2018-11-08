/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.auditlog;

import static com.gooddata.dataload.processes.ProcessType.DATALOAD;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import com.gooddata.CfalGoodData;
import com.gooddata.FutureResult;
import com.gooddata.dataload.processes.*;
import com.gooddata.project.Project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton for storing and manipulating with ETL processes and schedules.
 * Lazy initialized. Not thread safe.
 */
public class ProcessHelper {

    private static final Logger logger = LoggerFactory.getLogger(ProcessHelper.class);

    public static final String RUBY_SCRIPT_NAME = "test.rb";

    private static final String CLOVER_GRAPH_FOLDER_NAME = "test_grf";
    private static final String CLOVER_GRAPH_NAME = "test.grf";
    private static final String SCHEDULE_CRON_EXPRESSION = "0 0 * * *";
    private static final String APPSTORE_PATH = "${PUBLIC_APPSTORE}:branch/demo:/test/HelloApp";

    private static ProcessHelper instance;

    private List<DataloadProcess> processes = new ArrayList<>();
    private List<Schedule> schedules = new ArrayList<>();

    private final CfalGoodData gd;
    private final TestEnvironmentProperties props;

    private ProcessHelper(final CfalGoodData gd, final TestEnvironmentProperties props) {
        this.gd = gd;
        this.props = props;
    }

    public static ProcessHelper getInstance() {
        if (instance == null) {
            final CfalGoodData gd = CfalGoodData.getInstance();
            final TestEnvironmentProperties props = TestEnvironmentProperties.getInstance();
            instance = new ProcessHelper(gd, props);
        }
        return instance;
    }

    /**
     * Creates and returns new ETL Ruby process for the given project.
     * The created process contains a RUBY "Hello world" script.
     *
     * @param project GD project
     * @return Ruby ETL process
     */
    public DataloadProcess createRubyProcess(final Project project) throws URISyntaxException {
        notNull(project, "project cannot be null!");

        DataloadProcess process = gd.getProcessService().createProcess(project,
                new DataloadProcess(testProcessName(RUBY_SCRIPT_NAME), ProcessType.RUBY), getScriptFile(RUBY_SCRIPT_NAME));

        processes.add(process);

        return process;
    }

    /**
     * Creates and returns new ETL Clover process for the given project.
     * The created process contains simple GRAPH which generates some data and throws them away right after that.
     *
     * @param project GD project
     * @return Graph ETL process
     */
    public DataloadProcess createCloverProcess(final Project project) throws URISyntaxException {
        notNull(project, "project cannot be null!");

        final DataloadProcess process = gd.getProcessService().createProcess(project,
                new DataloadProcess(testProcessName(CLOVER_GRAPH_NAME), ProcessType.GRAPH),
                getScriptFile(CLOVER_GRAPH_FOLDER_NAME));

        processes.add(process);

        return process;
    }

    /**
     * Creates process from appstore for the given project.
     * Gets the default appstore path to the HelloApp RUBY script.
     *
     * @param project GD project
     * @return RUBY ETL process created from appstore
     */
    public DataloadProcess createProcessFromAppstore(final Project project) {
        notNull(project, "project cannot be null!");

        final DataloadProcess process = gd.getProcessService().createProcessFromAppstore(project,
                new DataloadProcess(testProcessName(APPSTORE_PATH), ProcessType.RUBY.toString(), APPSTORE_PATH)).get();

        logger.info("deployed process_id={} from appstore", process.getId());

        processes.add(process);

        return process;
    }

    /**
     * Updates ETL process.
     * This cannot update appstore process.
     */
    public void updateProcess(final DataloadProcess process) throws URISyntaxException {
        notNull(process, "process cannot be null!");

        gd.getProcessService().updateProcess(process, getScriptFile(getScriptNameForProcess(process)));
    }

    /**
     * Executes ETL process and returns its execution result.
     * This cannot execute appstore process.
     *
     * @param process ETL process to be executed
     * @return ETL process execution result
     */
    public ProcessExecutionDetail executeProcess(final DataloadProcess process) {
        notNull(process, "process cannot be null!");

        final ProcessExecution execution = new ProcessExecution(process, getScriptNameForProcess(process));
        final FutureResult<ProcessExecutionDetail> result = gd.getProcessService().executeProcess(execution);
        logger.info("Process execution uri={}", result.getPollingUri());
        return result.get(props.getPollTimeoutMinutes(), props.getPollTimeoutUnit());
    }

    /**
     * Removes ETL process from GD platform.
     */
    public void removeProcess(final DataloadProcess process) {
        notNull(process, "process cannot be null!");

        gd.getProcessService().removeProcess(process);
        processes.remove(process);
    }

    /**
     * Creates schedule for the given process in the given project.
     *
     * @param project GD project
     * @param process dataload process
     * @return new schedule
     */
    public Schedule createSchedule(final Project project, final DataloadProcess process) {
        notNull(project, "project cannot be null!");
        notNull(process, "process cannot be null!");

        final Schedule schedule = gd.getProcessService().createSchedule(project,
                new Schedule(process, RUBY_SCRIPT_NAME, SCHEDULE_CRON_EXPRESSION));

        schedules.add(schedule);

        return schedule;
    }

    /**
     * Creates ADD/DE schedule for the given project. Project must have OS defined before this.
     *
     * @param project GD project
     * @return new ADD schedule for synchronizing all datasets
     * @throws IllegalStateException when no DATALOAD process was found (OS not defined or process was removed manually)
     */
    public Schedule createADDSchedule(final Project project) {
        notNull(project, "project cannot be null!");

        final DataloadProcess dataloadProcess = gd.getProcessService()
                .listProcesses(project)
                .stream()
                .filter(e -> DATALOAD.name().equals(e.getType()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No DATALOAD process found for project=" + project.getId()));

        final Schedule schedule = new Schedule(dataloadProcess, null, SCHEDULE_CRON_EXPRESSION);
        schedule.addParam("GDC_DE_SYNCHRONIZE_ALL", "true");

        final Schedule createdSchedule = gd.getProcessService().createSchedule(project, schedule);
        schedules.add(createdSchedule);

        return createdSchedule;
    }

    /**
     * Executes schedule and returns its execution result.
     *
     * @param schedule schedule
     * @return schedule execution result
     */
    public ScheduleExecution executeSchedule(final Schedule schedule) {
        notNull(schedule, "schedule cannot be null!");

        final FutureResult<ScheduleExecution> result = gd.getProcessService().executeSchedule(schedule);
        logger.info("Schedule execution uri={}", result.getPollingUri());
        return result.get(props.getPollTimeoutMinutes(), props.getPollTimeoutUnit());
    }

    /**
     * Removes all schedules generated by this helper from GD platform.
     */
    public void clearAllSchedules() {
        schedules.forEach(schedule -> {
            try {
                logger.info("removing schedule_uri={}", schedule.getUri());
                gd.getProcessService().removeSchedule(schedule);
                logger.info("schedule_uri={} removed", schedule.getUri());
            } catch (Exception e) {
                logger.warn("could not remove schedule_uri=" + schedule.getUri(), e);
            }
        });
        schedules.clear();
    }

    /**
     * Returns file of the script contained in the process according of the script name
     *
     * @param scriptName the script name
     * @return {@link File} containing script for the ETL process
     * @throws URISyntaxException if the script file URL of this process cannot be converted to URI
     */
    public File getScriptFile(final String scriptName) throws URISyntaxException {
        notEmpty(scriptName, "scriptName cannot be empty!");

        final URL resource = getClass().getClassLoader().getResource(scriptName);
        if (resource == null) {
            throw new IllegalArgumentException("ETL process script '" + scriptName + " not found.");
        }

        return new File(resource.toURI());
    }

    /**
     * Removes all created processes and schedules. This should be called after the test suite.
     */
    public void destroy() {
        clearAllSchedules();
        processes.forEach(process -> {
            try {
                logger.info("removing process_uri={}", process.getUri());
                gd.getProcessService().removeProcess(process);
                logger.info("process_uri={} removed", process.getUri());
            } catch (Exception e) {
                logger.warn("could not remove process_uri=" + process.getUri(), e);
            }
        });
        processes.clear();
    }

    private String testProcessName(final String scriptName) {
        return "CFAL - ETL Process AT (" + scriptName + ")";
    }

    private String getScriptNameForProcess(final DataloadProcess process) {
        final ProcessType processType = ProcessType.valueOf(process.getType().toUpperCase());
        switch (processType) {
            case RUBY: return RUBY_SCRIPT_NAME;
            case GRAPH: return CLOVER_GRAPH_NAME;
            default: throw new IllegalArgumentException("File for process type '" + process.getType() + "' not defined");
        }
    }
}