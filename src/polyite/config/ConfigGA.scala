package polyite.config

import java.io.File
import java.util.Properties
import java.util.logging.Logger

import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION
import scala.math.BigInt.int2bigInt

import polyite.util.Rat
import polyite.evolution.GeneticOperatorFactory
import GeneticOperatorFactory.GeneticOperators
import polyite.config.MinimalConfig.NumGeneratorsLimit

object ConfigGA {
  val myLogger : Logger = Logger.getLogger("")

  def loadAndValidateConfig(f : File) : Option[ConfigGA] = {
    return parseConfig(MinimalConfig.loadProperties(f))
  }

  def parseConfig(rawConf : Properties) : Option[ConfigGA] = {
    val basicConf : Config = Config.parseConfig(rawConf) match {
      case None    => return None
      case Some(c) => c
    }

    var propName : String = "probabilityToMutateSchedRow"
    val probabilityToMutateSchedRow : Option[Double] = MinimalConfig.getDoubleProperty(
      propName, rawConf)
    if (!probabilityToMutateSchedRow.isDefined) return None
    if (!MinimalConfig.checkMinMax(0, 1, probabilityToMutateSchedRow, propName)) return None

    propName = "probabilityToMutateGeneratorCoeff"
    val probabilityToMutateGeneratorCoeff : Option[Double] = MinimalConfig.getDoubleProperty(
      propName, rawConf)
    if (!probabilityToMutateGeneratorCoeff.isDefined) return None
    if (!MinimalConfig.checkMinMax(0, 1, probabilityToMutateGeneratorCoeff, propName))
      return None

    propName = "generatorCoeffMaxDenominator"
    val generatorCoeffMaxDenominator : Option[Int] = MinimalConfig.getIntProperty(propName,
      rawConf)
    if (!generatorCoeffMaxDenominator.isDefined) return None
    if (!MinimalConfig.checkMin(1, generatorCoeffMaxDenominator, propName)) return None

    propName = "currentGeneration"
    val currentGeneration : Option[Int] = MinimalConfig.getIntProperty(propName, rawConf)
    if (!currentGeneration.isDefined) return None
    if (!MinimalConfig.checkMin(0, currentGeneration, propName)) return None

    propName = "maxGenerationToReach"
    val maxGenerationToReach : Option[Int] = MinimalConfig.getIntProperty(propName, rawConf)
    if (!maxGenerationToReach.isDefined) return None
    if (!MinimalConfig.checkMin(currentGeneration.get + 1, maxGenerationToReach, propName)) return None

    propName = "regularPopulationSize"
    val regularPopulationSize : Option[Int] = MinimalConfig.getIntProperty(propName, rawConf)
    if (!regularPopulationSize.isDefined) return None
    if (!MinimalConfig.checkMin(1, regularPopulationSize, propName)) return None

    propName = "fractionOfSchedules2Keep"
    val fractionOfSchedules2Keep : Option[Rat] = MinimalConfig.getRatProperty(propName, rawConf)
    if (!fractionOfSchedules2Keep.isDefined)
      return None
    if (fractionOfSchedules2Keep.get <= Rat(0)) {
      myLogger.warning("fractionOfSchedules2Keep must not be <= 0: "
        + fractionOfSchedules2Keep)
      return None
    }

    propName = "maxNumNewSchedsFromCrossover"
    val maxNumNewSchedsFromCrossover : Option[Int] = MinimalConfig.getIntProperty(propName, rawConf)
    if (!maxNumNewSchedsFromCrossover.isDefined)
      return None

    propName = "optimizeParallelExecTime"
    val optimizeParallelExecTime : Option[Boolean] = MinimalConfig.getBooleanProperty(propName, rawConf)
    if (!optimizeParallelExecTime.isDefined)
      return None

    propName = "evolutionTimeout"
    val evolutionTimeout : Option[Long] = MinimalConfig.getLongProperty(propName, rawConf)
    if (!evolutionTimeout.isDefined)
      return None

    propName = "shareOfRandSchedsInPopulation"
    val shareOfRandSchedsInPopulation : Option[Rat] = MinimalConfig.getRatProperty(propName,
      rawConf)
    if (!shareOfRandSchedsInPopulation.isDefined)
      return None
    if (shareOfRandSchedsInPopulation.get < Rat(0)) {
      myLogger.warning("shareOfRandSchedsInPopulation nust not be negative: "
        + shareOfRandSchedsInPopulation.get)
      return None
    }

    if (!optimizeParallelExecTime.get && !basicConf.measureSeqExecTime) {
      myLogger.warning("Cannot optimize sequential execution time while "
        + "sequential execution is disabled.")
      return None
    }

    val activeMutators : List[GeneticOperators.Value] = GeneticOperatorFactory.
      GeneticOperators.values.filter { (op : GeneticOperators.Value) =>
        {
          var enabled = false
          if (GeneticOperatorFactory.GeneticOperators.isMutator(op)) {
            propName = op + "Enabled"
            val opEnabled : Option[Boolean] = MinimalConfig.getBooleanProperty(propName, rawConf)
            if (!opEnabled.isDefined)
              return None
            enabled = opEnabled.get
          }
          enabled
        }
      } toList

    val activeCrossovers : List[GeneticOperators.Value] = GeneticOperatorFactory.
      GeneticOperators.values.filter { (op : GeneticOperators.Value) =>
        {
          var enabled = false
          if (!GeneticOperatorFactory.GeneticOperators.isMutator(op)) {
            propName = op + "Enabled"
            val opEnabled : Option[Boolean] = MinimalConfig.getBooleanProperty(propName, rawConf)
            if (!opEnabled.isDefined)
              return None
            enabled = opEnabled.get
          }
          enabled
        }
      } toList

    if (activeMutators.isEmpty && activeCrossovers.isEmpty) {
      myLogger.warning("All genetic operators are disabled.")
      return None
    }

    val initPopulationNumRays : Option[NumGeneratorsLimit] = MinimalConfig.parseNumGeneratorsLimit("initPopulationNumRays", rawConf)
    if (!initPopulationNumRays.isDefined) return None

    val initPopulationNumLines : Option[NumGeneratorsLimit] = MinimalConfig.parseNumGeneratorsLimit("initPopulationNumLines", rawConf)
    if (!initPopulationNumLines.isDefined) return None

    propName = "useConvexAnnealingFunction"
    val useConvexAnnealingFunction : Option[Boolean] = MinimalConfig.getBooleanProperty(propName, rawConf)
    if (!useConvexAnnealingFunction.isDefined)
      return None

    return Some(new ConfigGA(
      basicConf.numMeasurementThreads,
      basicConf.rayCoeffsRange,
      basicConf.lineCoeffsRange,
      basicConf.maxNumRays,
      basicConf.maxNumLines,
      basicConf.probabilityToCarryDep,
      basicConf.maxNumSchedsAtOnce,
      basicConf.measurementCommand,
      basicConf.measurementWorkingDir,
      basicConf.measurementTmpDirBase,
      basicConf.benchmarkName,
      basicConf.functionName,
      basicConf.scopRegionStart,
      basicConf.scopRegionEnd,
      basicConf.irFilesLocation,
      basicConf.referenceOutputFile,
      basicConf.numExecutionTimeMeasurements,
      basicConf.populationFilePrefix,
      basicConf.exportSchedulesToJSCOPFiles,
      basicConf.jscopFolderPrefix,
      basicConf.measurementTimeout,
      basicConf.exportPopulationToCSV,
      basicConf.csvFilePrefix,
      basicConf.logToFile,
      basicConf.logFile,
      basicConf.evaluationSigIntExitCode,
      basicConf.randSchedsTimeout,
      basicConf.measurementTmpDirNamePrefix,
      basicConf.genSchedsMaxAllowedConseqFailures,
      basicConf.numScheduleGenThreads,
      basicConf.filterImportedPopulation,
      basicConf.islComputeout,
      basicConf.paramValMappings,
      basicConf.measureParExecTime,
      basicConf.measureSeqExecTime,
      basicConf.moveVertices,
      basicConf.rayPruningThreshold,
      basicConf.seqPollyOptFlags,
      basicConf.parPollyOptFlags,
      basicConf.insertSetNodes,
      basicConf.compilationTimeout,
      basicConf.benchmarkingSurrenderTimeout,
      basicConf.measureCacheHitRateSeq,
      basicConf.measureCacheHitRatePar,
      basicConf.seed,
      basicConf.numactlConf,
      basicConf.linIndepVectsDoNotFixDims,
      basicConf.simplifySchedTrees,
      basicConf.splitLoopBodies,
      basicConf.numCompilatonDurationMeasurements,
      basicConf.validateOutput,
      basicConf.tilingPermitInnerSeq,
      basicConf.schedTreeSimplRebuildDimScheds,
      basicConf.schedTreeSimplRemoveCommonOffset,
      basicConf.schedTreeSimplDivideCoeffsByGCD,
      basicConf.schedTreeSimplElimSuperfluousSubTrees,
      basicConf.schedTreeSimplElimSuperfluousDimNodes,
      basicConf.barvinokBinary,
      basicConf.barvinokLibraryPath,
      basicConf.normalizeFeatures,
      basicConf.gpu,

      probabilityToMutateSchedRow.get,
      probabilityToMutateGeneratorCoeff.get,
      generatorCoeffMaxDenominator.get,
      currentGeneration.get,
      maxGenerationToReach.get,
      regularPopulationSize.get,
      fractionOfSchedules2Keep.get,
      maxNumNewSchedsFromCrossover.get,
      optimizeParallelExecTime.get,
      evolutionTimeout.get,
      shareOfRandSchedsInPopulation.get,
      activeMutators,
      activeCrossovers,
      initPopulationNumRays.get,
      initPopulationNumLines.get,
      useConvexAnnealingFunction.get))
  }
}

class ConfigGA(
  numMeasurementThreads : Int,
  rayCoeffsRange : Int,
  lineCoeffsRange : Int,
  maxNumRays : MinimalConfig.NumGeneratorsLimit,
  maxNumLines : MinimalConfig.NumGeneratorsLimit,
  probabilityToCarryDep : Double,
  maxNumSchedsAtOnce : Int,
  measurementCommand : String,
  measurementWorkingDir : File,
  measurementTmpDirBase : File,
  benchmarkName : String,
  functionName : String,
  scopRegionStart : String,
  scopRegionEnd : String,
  irFilesLocation : File,
  referenceOutputFile : File,
  numExecutionTimeMeasurements : Int,
  populationFilePrefix : String,
  exportSchedulesToJSCOPFiles : Boolean,
  jscopFolderPrefix : String,
  measurementTimeout : Long,
  exportPopulationToCSV : Boolean,
  csvFilePrefix : String,
  logToFile : Boolean,
  logFile : File,
  evaluationSigIntExitCode : Int,
  randSchedsTimeout : Long,
  measurementTmpDirNamePrefix : String,
  genSchedsMaxAllowedConseqFailures : Int,
  numScheduleGenThreads : Int,
  filterImportedPopulation : Boolean,
  islComputeout : Int,
  paramValMappings : Map[String, Int],
  measureParExecTime : Boolean,
  measureSeqExecTime : Boolean,
  moveVertices : Boolean,
  rayPruningThreshold : Option[Rat],
  seqPollyOptFlags : String,
  parPollyOptFlags : String,
  insertSetNodes : Boolean,
  compilationTimeout : Option[Long],
  benchmarkingSurrenderTimeout : Option[Double],
  measureCacheHitRateSeq : Boolean,
  measureCacheHitRatePar : Boolean,
  seed : Option[Long],
  numactlConf : Option[String],
  linIndepVectsDoNotFixDims : Boolean,
  simplifySchedTrees : Boolean,
  splitLoopBodies : Boolean,
  numCompilatonDurationMeasurements : Int,
  validateOutput : Boolean,
  tilingPermitInnerSeq : Boolean,
  schedTreeSimplRebuildDimScheds : Boolean,
  schedTreeSimplRemoveCommonOffset : Boolean,
  schedTreeSimplDivideCoeffsByGCD : Boolean,
  schedTreeSimplElimSuperfluousSubTrees : Boolean,
  schedTreeSimplElimSuperfluousDimNodes : Boolean,
  barvinokBinary : File,
  barvinokLibraryPath : File,
  normalizeFeatures : Boolean,
  gpu : Boolean,

  val probabilityToMutateSchedRow : Double,
  val probabilityToMutateGeneratorCoeff : Double,
  val generatorCoeffMaxDenominator : Int,
  val currentGeneration : Int,
  val maxGenerationToReach : Int,
  val regularPopulationSize : Int,
  val fractionOfSchedules2Keep : Rat,
  val maxNumNewSchedsFromCrossover : Int,
  val optimizeParallelExecTime : Boolean,
  val evolutionTimeout : Long,
  val shareOfRandSchedsInPopulation : Rat,
  val activeMutators : List[GeneticOperatorFactory.GeneticOperators.Value],
  val activeCrossovers : List[GeneticOperatorFactory.GeneticOperators.Value],
  val initPopulationNumRays : MinimalConfig.NumGeneratorsLimit,
  val initPopulationNumLines : MinimalConfig.NumGeneratorsLimit,
  val useConvexAnnealingFunction : Boolean) extends Config(
  numMeasurementThreads,
  rayCoeffsRange,
  lineCoeffsRange,
  maxNumRays,
  maxNumLines,
  probabilityToCarryDep,
  maxNumSchedsAtOnce,
  measurementCommand,
  measurementWorkingDir,
  measurementTmpDirBase,
  benchmarkName,
  functionName,
  scopRegionStart,
  scopRegionEnd,
  irFilesLocation,
  referenceOutputFile,
  numExecutionTimeMeasurements,
  populationFilePrefix,
  exportSchedulesToJSCOPFiles,
  jscopFolderPrefix,
  measurementTimeout,
  exportPopulationToCSV,
  csvFilePrefix,
  logToFile,
  logFile,
  evaluationSigIntExitCode,
  randSchedsTimeout,
  measurementTmpDirNamePrefix,
  genSchedsMaxAllowedConseqFailures,
  numScheduleGenThreads,
  filterImportedPopulation,
  islComputeout,
  paramValMappings,
  measureParExecTime,
  measureSeqExecTime,
  moveVertices,
  rayPruningThreshold,
  seqPollyOptFlags,
  parPollyOptFlags,
  insertSetNodes,
  compilationTimeout,
  benchmarkingSurrenderTimeout,
  measureCacheHitRateSeq,
  measureCacheHitRatePar,
  seed,
  numactlConf,
  linIndepVectsDoNotFixDims,
  simplifySchedTrees,
  splitLoopBodies,
  numCompilatonDurationMeasurements,
  validateOutput,
  tilingPermitInnerSeq,
  schedTreeSimplRebuildDimScheds,
  schedTreeSimplRemoveCommonOffset,
  schedTreeSimplDivideCoeffsByGCD,
  schedTreeSimplElimSuperfluousSubTrees,
  schedTreeSimplElimSuperfluousDimNodes,
  barvinokBinary,
  barvinokLibraryPath,
  normalizeFeatures,
  gpu) {

  override def toString() : String = {
    val sb : StringBuilder = StringBuilder.newBuilder
    sb.append(super.toString())
    MinimalConfig.toStringAppend("probabilityToMutateSchedRow", probabilityToMutateSchedRow, sb)
    MinimalConfig.toStringAppend("probabilityToMutateGeneratorCoeff", probabilityToMutateGeneratorCoeff, sb)
    MinimalConfig.toStringAppend("generatorCoeffMaxDenominator", generatorCoeffMaxDenominator, sb)
    MinimalConfig.toStringAppend("currentGeneration", currentGeneration, sb)
    MinimalConfig.toStringAppend("maxGenerationToReach", maxGenerationToReach, sb)
    MinimalConfig.toStringAppend("regularPopulationSize", regularPopulationSize, sb)
    MinimalConfig.toStringAppend("fractionOfSchedules2Keep", fractionOfSchedules2Keep, sb)
    MinimalConfig.toStringAppend("maxNumNewSchedsFromCrossover", maxNumNewSchedsFromCrossover, sb)
    MinimalConfig.toStringAppend("optimizeParallelExecTime", optimizeParallelExecTime, sb)
    MinimalConfig.toStringAppend("evolutionTimeout", evolutionTimeout, sb)
    MinimalConfig.toStringAppend("shareOfRandSchedsInPopulation", shareOfRandSchedsInPopulation, sb)
    MinimalConfig.toStringAppend("initPopulationNumRays", initPopulationNumRays, sb)
    MinimalConfig.toStringAppend("initPopulationNumLines", initPopulationNumLines, sb)
    MinimalConfig.toStringAppend("useConvexAnnealingFunction", useConvexAnnealingFunction, sb)

    for (op <- GeneticOperators.values) {
      if (GeneticOperators.isMutator(op)) {
        sb.append(op).append("Enabled=").append(activeMutators.contains(op)).append('\n')
      }
    }
    for (op <- GeneticOperators.values) {
      if (!GeneticOperators.isMutator(op)) {
        sb.append(op).append("Enabled=").append(activeCrossovers.contains(op)).append('\n')
      }
    }

    return sb.toString()
  }
}