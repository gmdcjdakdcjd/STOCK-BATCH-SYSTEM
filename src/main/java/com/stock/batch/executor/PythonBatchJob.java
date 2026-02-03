package com.stock.batch.executor;

import com.stock.batch.domain.PythonJobResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Slf4j
@Component
public class PythonBatchJob {

    @Value("${python.exec.cmd}")
    private String pythonCmd;

    @Value("${python.workdir}")
    private String workDir;

    @Value("${python.path}")
    private String pythonPath;

    @Value("${python.encoding:utf-8}")
    private String encoding;

    @Value("${app.env}")
    private String appEnv;

    public PythonJobResult runPythonScript(String scriptPath) {

        try {
            log.info("Python batch start: {}", scriptPath);

            ProcessBuilder pb = new ProcessBuilder(
                    pythonCmd,
                    scriptPath
            );

            pb.directory(new File(workDir));

            //이 한 줄이 핵심
            pb.environment().put("APP_ENV", appEnv);

            pb.environment().put("PYTHONPATH", pythonPath);
            pb.environment().put("PYTHONIOENCODING", encoding);
            pb.redirectErrorStream(true);

            Process process = pb.start();

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(process.getInputStream(), encoding)
                    );

            String line;
            int rowCount = 0;
            int codeCount = 0;
            Long resultId = null;

            while ((line = reader.readLine()) != null) {
                log.info("[PYTHON] {}", line);

                if (line.startsWith("ROWCOUNT=")) {
                    rowCount = Integer.parseInt(line.split("=")[1].trim());
                } else if (line.startsWith("CODECOUNT=")) {
                    codeCount = Integer.parseInt(line.split("=")[1].trim());
                } else if (line.startsWith("RESULT_ID=")) {
                    resultId = Long.parseLong(line.split("=")[1].trim());
                }
            }

            int exitCode = process.waitFor();

            log.info("Python batch end (exitCode={})", exitCode);

            return exitCode == 0
                    ? new PythonJobResult("SUCCESS", rowCount, codeCount, null, resultId)
                    : new PythonJobResult("FAIL", rowCount, codeCount, "EXIT=" + exitCode, resultId);

        } catch (Exception e) {
            log.error("Python batch failed", e);
            return new PythonJobResult("FAIL", 0, 0, e.getMessage(), null);
        }
    }
}
