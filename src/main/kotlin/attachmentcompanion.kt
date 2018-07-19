import com.securemessaging.SecureMessenger
import com.securemessaging.sm.Credentials
import com.securemessaging.sm.Message
import cryptor.AESSymmetricCryptor
import cryptor.DESSymmetricCryptor
import cryptor.SymmetricCryptor
import utils.ArgParser
import utils.UserConfiguration
import java.io.File
import java.util.*


fun main(params:Array<String>){

    val argParser = ArgParser(params)

    if(argParser.keyExists("--HELP") || argParser.keyExists("--help")
    || argParser.keyExists("-h") || argParser.keyExists("-H")){
        printHelp()
        return
    }

    val mode = argParser.getValue("-m")
    if(mode == null){
        println("Mode is required in order to determine processing action. See help for details")

        return
    }

    when(mode.toLowerCase()){
        "upload" ->{
            executeUploadProcess(argParser)
        }

        "download" ->{
            executeDownloadProcess(argParser)
        }
        else ->{
            println("Invalid Mode Supplied. Please pass either 'upload' or 'download' as a mode. See help for details")
            return
        }
    }

    print("Processing Complete. Terminating")
}

fun printHelp(){

    println("========================================================================================================")
    println("                                    Attachment Companion                                                ")
    println("                                            v1.1.0                                                      ")
    println("========================================================================================================")
    println("Usage:")
    println("\tjava -jar ./attachmentCompanion.jar -m <upload|download> -g <messageGuid> -f <file/folder path> \n" +
            "\t-k <encryption/decryption key> -u <username> -p <password> -s <servicecode> -e <AES|DES>")


    println("Parameters:")
    println("\t-m\t[REQUIRED]\t\tSpecify the mode. Can be either 'upload' or 'download'\n" +
            "\t  \t\t\t\t\tdepending on mode changes other parameter uses")
    println("\t-u\t[REQUIRED]\t\tProvide username to the secure messaging platform.")
    println("\t-p\t[REQUIRED]\t\tProvide password to the secure messaging platform.")
    println("\t-s\t[REQUIRED]\t\tProvide the service code to the secure messaging\n" +
            "\t  \t\t\t\t\tplatform.")
    println("\t-g\t[OPTIONAL]\t\tSpecify the messageGuid the attachments will be\n" +
            "\t  \t\t\t\t\tuploaded to in 'upload' mode, downloaded from in 'download'\n" +
            "\t  \t\t\t\t\tmode. If no messageGuid is supplied on 'upload'\n" +
            "\t  \t\t\t\t\tthen a message will be created automatically. If no messageGuid\n" +
            "\t  \t\t\t\t\tis supplied on 'download' but a decryption key is supplied.\n" +
            "\t  \t\t\t\t\tPreviously downloaded but not decrypted files will be scanned\n" +
            "\t  \t\t\t\t\tand decrypted")

    println("\t-f\t[REQUIRED]\t\tSpecify the file or folder of files to be uploaded\n" +
            "\t  \t\t\t\t\tin 'upload' mode. Specify the download folder in download' mode")
    println("\t-k\t[REQUIRED]\t\tSpecify the encryption/decryption password. If\n" +
            "\t  \t\t\t\t\tsupplied in 'upload' the attachment will be encrypted\n" +
            "\t  \t\t\t\t\tbefore uploaded. If supplied in 'download' the attachment\n" +
            "\t  \t\t\t\t\twill be decrypted after being downloaded")
    println("\t-e\t[OPTIONS]\t\tSpecify encryption mode. Can either 'AES' or 'DES'. Default is 'AES'")


    println("Flags:")
    println("\t--HELP\t\tPrint this help information")


    println("Examples:")
    println("\tjava -jar ./attachmentCompanion.jar -m upload -g 2d07394a-5b29-4fd3-b6f7-fcc09d7d8ecb -f C:/myfile.txt -k myencryptpassword\n" +
            "\t-u john.smith@deliveryslip.com -p mymailpass -s secure")
    println("\t - Encrypts myfile.txt using myencryptpassword and uploads it to the secure message in the 'secure' portal\n" +
            "\t   authenticating with john.smith@deliveryslip.com and password mymailpass")
    println()
    println("\tjava-jar ./attachmentCompanion.jar -m download -g 2d07394a-5b29-4fd3-b6f7-fcc09d7d8ecb -f C:/downloads -k myencryptpassword\n" +
            "\t-u john.smith@deliveryslip.com -p mymailpass -s secure")
    println("\t - Decrypts all valid encrypted files downloaded from the secure message into the C:/downloads folder\n" +
            "\t   using 'myencryptpassword' to decrypt and john.smith@deliveryslip and mymailpass to authenticate")
    println()
    println("\t java -jar ./attachmentCompanion.jar -m download -f C:/downloads -k myencryptpassword")
    println("\t - Decrypts all valid files in C:/downloads using the myencryptpassword. Useful if you have already\n" +
            "\t downloaded encrypted attachments")

}

fun executeUploadProcess(argParser: ArgParser){

    println("Upload Mode Detected. Executing Upload Processing...")

    UserConfiguration.loadUserConfiguration(argParser)
    UserConfiguration.validateUserConfiguration()

    var strMessageGuid = argParser.getValue("-g")

    val messenger = SecureMessenger.resolveViaServiceCode(UserConfiguration.serviceCode)
    val credentals = Credentials(UserConfiguration.username, UserConfiguration.password)
    messenger.login(credentals)

    val message: Message

    if(strMessageGuid == null){
        println("No Message Guid Detected. Generating A Message And Saving Into Drafts")

        message = messenger.preCreateMessage()
        message.subject = "Attachment Companion Generated Message"
        messenger.saveMessage(message)

        println("Message With GUID: >${message.messageGuid}< Has Been Created And Saved Into Drafts")
    }else{
        message = messenger.getMessage(strMessageGuid)
    }

    val attachmentManager = messenger.createAttachmentManagerForMessage(message)

    val encryptionPassword = argParser.getValue("-k")
    var symmetricCryptor:SymmetricCryptor? = null
    if(encryptionPassword != null){
        println("Encryption Password Detected. Attachments Will Be Encrypted Before Uploaded")

        val cryptoType = argParser.getValue("-e") ?: "AES"
        println("Encrypting Using $cryptoType Algorithm")

        when(cryptoType.toLowerCase()){
            "aes" ->{
                symmetricCryptor = AESSymmetricCryptor(encryptionPassword)
            }
            "des" ->{
                symmetricCryptor = DESSymmetricCryptor(encryptionPassword)
            }
        }
    }

    val fileLocationString = argParser.getValue("-f")
    val fileLocationFile = File(fileLocationString)
    if(!fileLocationFile.exists()){
        println("File Parameter Location Does Not Exist. Terminating")
        return
    }

    val fileList = ArrayList<File>()

    if(fileLocationFile.isDirectory){
        fileLocationFile.listFiles().forEach {
            fileList.add(it)
        }
    }else if(fileLocationFile.isFile){
        fileList.add(fileLocationFile)
    }else{
        println("File Parameter Could Not Be Determined If Is Directory or File. Terminating")
        return
    }

    fileList.forEach{

        val uploadedFile:File

        if(encryptionPassword != null) {

            val fileContents = symmetricCryptor!!.encrypt(it.readBytes())
            //write the encrypted attachment to file
            val cipherFileLocation = it.parent + "/${it.name}.enc"
            println("Encrypting Attachment: ${it.name}")
            println("Generating Cipher File At Location $cipherFileLocation")
            uploadedFile = File(cipherFileLocation)
            uploadedFile.createNewFile()
            uploadedFile.writeBytes(fileContents)
        }else{
            uploadedFile = it
        }

        println("Adding Attachment ${uploadedFile.name}")
        //upload the encrypted attachment to the message
        attachmentManager.addAttachmentFile(uploadedFile)

    }

    println("Now PreCreating All Attachments")
    attachmentManager.preCreateAllAttachments()
    println("Now Uploading All Attachments")
    attachmentManager.uploadAllAttachments()
    println("Upload Of Attachments Complete")

}

fun executeDownloadProcess(argParser: ArgParser){

    //give a message guid to grab its attachments OR give a folder location which will be scanned and decrypted
    //optionally giving a messageGuid you can also still give a folder location which is where the attachments
    //will be downloaded to before they are decrypted

    println("Download Mode Detected. Executing Download Processing...")

    var strMessageGuid = argParser.getValue("-g")
    var folderLocation = argParser.getValue("-f")

    val encryptionPassword = argParser.getValue("-k")

    //check one fo the two were supplied
    if(strMessageGuid == null && folderLocation == null){
        println("Attachment Companion Can't Run Without A Valid Message Guid OR Folder Location." +
                " See help for details")
        return
    }

    //if the folder location was supplied then make sure its a directory
    if(folderLocation != null && !File(folderLocation).isDirectory){
        println("Folder Location Parameter must belong to a valid directory in order to download properly")
        return
    }

    if(strMessageGuid != null){
        println("Message Guid Detected. Fetching Message From The Server")
        //message guid is present - so we are getting this from the server

        UserConfiguration.loadUserConfiguration(argParser)
        UserConfiguration.validateUserConfiguration()

        strMessageGuid = strMessageGuid.replace("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})".toRegex(), "$1-$2-$3-$4-$5")

        val messenger = SecureMessenger.resolveViaServiceCode(UserConfiguration.serviceCode)
        val credentials = Credentials(UserConfiguration.username, UserConfiguration.password)
        messenger.login(credentials)

        val message = messenger.getMessage(strMessageGuid)
        val attachmentManager = messenger.createAttachmentManagerForMessage(message);

        //by default we will download to a downloads folder - otherwise user can optionally pass a folder and well put
        //it there
        if(folderLocation == null){
            folderLocation = "./downloads"
        }

        //download attachments belonging to message
        //get the message to get the attachment information
        println("Now Downloading Files From The Server")
        attachmentManager.attachmentsInfo.forEach{
            attachmentManager.downloadAttachment(it, folderLocation)
        }
    }

    if(encryptionPassword != null){
        println("Encryption Password Detected. Determining Algorithm and then Analyzing Downloaded Attachments")

        val cryptoType = argParser.getValue("-e") ?: "AES"
        var symmetricCryptor:SymmetricCryptor? = null
        when(cryptoType){
            "AES" ->{
                symmetricCryptor = AESSymmetricCryptor(encryptionPassword)
            }
            "DES" ->{
                symmetricCryptor = DESSymmetricCryptor(encryptionPassword)
            }
        }

        //if they have .enc in their name - decrypt them
        val folder = File(folderLocation)
        folder.listFiles().forEach {
            if(it.isFile && it.name.contains(".enc")){
                //this is an encrypted file
                println("Encrypted Downloaded Attachment Found! ${it.canonicalPath}. Decrypting")

                val encryptedFile = File(it.canonicalPath)
                val plaintext = symmetricCryptor!!.decrypt(encryptedFile.readBytes())
                val plaintextFileLocation = encryptedFile.parent + "/${encryptedFile.nameWithoutExtension}"
                println("Decrypting File To: $plaintextFileLocation")

                val plaintextFile = File(plaintextFileLocation)
                plaintextFile.createNewFile()
                plaintextFile.writeBytes(plaintext)

            }
        }
    }
}