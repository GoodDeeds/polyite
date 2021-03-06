package polyite

import polyite.config.Config
import java.util.Properties

class AbstractTest {
  protected def createTestConfig() : Option[Config] = {
    val props : Properties = new Properties()
    props.setProperty("numMeasurementThreads", "1")
    props.setProperty("rayCoeffsRange", "3")
    props.setProperty("lineCoeffsRange", "3")
    props.setProperty("maxNumRays", "2")
    props.setProperty("maxNumLines", "2")
    props.setProperty("probabilityToCarryDep", "0.4")
    props.setProperty("maxNumSchedsAtOnce", "1")
    props.setProperty("measurementCommand", "/home/stg/workspace/schedule-optimization/measure_polybench.bash")
    props.setProperty("measurementWorkingDir", "/home/stg/workspace/schedule-optimization/")
    props.setProperty("measurementTmpDirBase", "/tmp/")
    props.setProperty("measurementTmpDirNamePrefix", "test")
    props.setProperty("benchmarkName", "test")
    props.setProperty("functionName", "test")
    props.setProperty("scopRegionStart", "test.start")
    props.setProperty("scopRegionEnd", "test.end")
    props.setProperty("irFilesLocation", "/home/stg/workspace/schedule-optimization/polybench-c-4.1")
    props.setProperty("referenceOutputFile", "/dev/null")
    props.setProperty("numExecutionTimeMeasurements", "5")
    props.setProperty("populationFilePrefix", "test")
    props.setProperty("exportSchedulesToJSCOPFiles", "true")
    props.setProperty("jscopFolderPrefix", "test")
    props.setProperty("csvFilePrefix", "test")
    props.setProperty("measurementTimeout", "1")
    props.setProperty("exportPopulationToCSV", "true")
    props.setProperty("logToFile", "true")
    props.setProperty("logFile", "test.log")
    props.setProperty("evaluationSigIntExitCode", "42")
    props.setProperty("randSchedsTimeout", "200")
    props.setProperty("genSchedsMaxAllowedConseqFailures", "1000")
    props.setProperty("numScheds", "1")
    props.setProperty("numScheduleGenThreads", "4")
    props.setProperty("filterImportedPopulation", "false")
    props.setProperty("importScheds", "false")
    props.setProperty("islComputeout", "38400000")
    props.setProperty("barvinokBinary", "/home/stg/workspace/count_integer_points/count_integer_points")
    props.setProperty("barvinokLibraryPath", "/home/stg/workspace/barvinok/barvinok/install/lib")
    props.setProperty("paramValMappings", "n=1000")
    props.setProperty("measureParExecTime", "true")
    props.setProperty("measureSeqExecTime", "false")
    props.setProperty("boundSchedCoeffs", "true")
    props.setProperty("moveVertices", "false")
    props.setProperty("rayPruningThreshold", "NONE")
    props.setProperty("seqPollyOptFlags", "-polly-parallel=false -polly-vectorizer=none -polly-tiling=false -polly-process-unprofitable=true")
    props.setProperty("parPollyOptFlags", "-polly-parallel=true -polly-vectorizer=none -polly-tiling=true -polly-default-tile-size=64 -polly-process-unprofitable=true")
    props.setProperty("simplifySchedules", "false")
    props.setProperty("insertSetNodes", "false")
    props.setProperty("compilationTimeout", "300")
    props.setProperty("benchmarkingSurrenderTimeout", "172800")
    props.setProperty("measureCacheHitRatePar", "false")
    props.setProperty("measureCacheHitRateSeq", "false")
    props.setProperty("seed", "NONE")
    props.setProperty("numactlConf", "NONE")
    props.setProperty("linIndepVectsDoNotFixDims", "false")
    props.setProperty("simplifySchedTrees", "true")
    props.setProperty("splitLoopBodies", "true")
    props.setProperty("numCompilatonDurationMeasurements", "1")
    props.setProperty("validateOutput", "true")
    props.setProperty("tilingPermitInnerSeq", "false")
    props.setProperty("schedTreeSimplRebuildDimScheds", "true")
    props.setProperty("schedTreeSimplRemoveCommonOffset", "true")
    props.setProperty("schedTreeSimplDivideCoeffsByGCD", "true")
    props.setProperty("schedTreeSimplElimSuperfluousSubTrees", "true")
    props.setProperty("schedTreeSimplElimSuperfluousDimNodes", "true")
    return Config.parseConfig(props)
  }
}