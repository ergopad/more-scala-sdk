package sdk.experimental

import org.ergoplatform.appkit.config.ErgoToolConfig
import org.ergoplatform.appkit.impl.{ErgoScriptContract, ErgoTreeContract}
import org.ergoplatform.appkit.{Address, ConstantsBuilder, InputBox, NetworkType, Parameters, RestApiErgoClient, SecretString}

import scala.collection.JavaConverters.seqAsJavaListConverter

object Playground extends App {
  /**
   * What is this?
   *
   * Ans: gets all boxes from the always true smart contract and
   * sends the erg to my address. uwu.
   */
  // config
  val conf = ErgoToolConfig.load("config.json")
  // constants
  val NODE_URL = conf.getParameters.get("node_url")
  val NODE_API_KEY = conf.getParameters.get("node_api_key")
  val MNEMONIC = conf.getParameters.get("mnemonic")
  val ADDRESS = conf.getParameters.get("address")

  // setup client
  val client = RestApiErgoClient.create(
    NODE_URL,
    NetworkType.MAINNET,
    NODE_API_KEY,
    RestApiErgoClient.defaultMainnetExplorerUrl
  )
  // my address
  val address = Address.create(ADDRESS)

  // load and compile contract
  val script =
    s"""
       |sigmaProp(1 == 1)
       |""".stripMargin

  val ergoContract = ErgoScriptContract.create(ConstantsBuilder.create().build(), script, NetworkType.MAINNET)
  val contractAddress = ergoContract.toAddress

  client.execute(ctx => {
    // model inputs
    val boxIds = ctx.getUnspentBoxesFor(contractAddress, 0, 100)
      .toArray()
      .map(box => box.asInstanceOf[InputBox].getId.toString)
    val inputBoxes = ctx.getBoxesById(boxIds:_*)
    val totalErgs = inputBoxes.map(box => box.getValue).reduce((a, c) => a + c)

    // model outputs
    val outBox = ctx.newTxBuilder().outBoxBuilder()
      .value(totalErgs - Parameters.MinFee)
      .contract(address.toErgoContract)
      .build()

    // build unsigned tx
    val unsignedTx = ctx.newTxBuilder()
      .boxesToSpend(inputBoxes.toSeq.asJava)
      .outputs(outBox)
      .fee(Parameters.MinFee)
      .sendChangeTo(address.getErgoAddress)
      .build()

    // prover
    val prover = ctx.newProverBuilder
      .withMnemonic(
        SecretString.create(MNEMONIC),
        SecretString.empty(),
      )
      .withEip3Secret(0)
      .build()

    // sign and submit tx
    val signedTx = prover.sign(unsignedTx)
    val txId = ctx.sendTransaction(signedTx)
    println("Transaction Id:", txId)
  })
}
