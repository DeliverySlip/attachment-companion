# attachment-companion
Attachment companion is an attachment helper tool for bulk uploads and downloads of attachments from messages using
the Secure Messaging API.

The attachment companion also comes with options for the user to encrypt and decrypt their attachments on the client
side using either AES or DES and their own password

The Attachment Companion is built using the `secure-messaging-java` sample SDK and serves also as a useful demonstration
of the use of the API. The application is written in Kotlin and uses Gradle to manage its dependencies. The
application takes advantage of the `secure-messaging-java` sample SDKs support of jitpack to import the library.

# Prerequisites
To compile and execute the project you will need Java 8 installed. Attachment companion should be compatible with
Java 9 but has not been tested with it.

#Setup
To compile the Attachment companion, execute the following command from the root of the project
```$xslt
gradlew.bat mainJar    # windows
gradlew mainJar        # *nix
```
This will build the executable fat-jar in the `build/libs` folder. You can then execute the jar with the following
command
```$xslt
java -jar ./attachmentCompanion-1.0.jar -h
```
This will print out the help, describing all options available. Some of the basics are also described below

# Usage
Attachment Companion allows for bulk uploading and downloading of files. The user passes a folder location which is
then scanned for all files in to upload. Depending on parameters given, the Attachment Companion will then upload
these files to an already existing message, or create a new one and save it in the drafts folder of the account.

Below lists some of the common options and features available in the Attachment Companion

### Upload Bulk Attachments
```$xslt
java -jar ./attachmentCompanion-1.0.jar -m upload -f C:\files -u username@domain.com -p secretpassword -s mysuperservice
```
The above command will have the attachment companion create a new email for `username@domain.com` with password
`secretpassword` on their portal `mysuperservice`. It will then upload all files found in the `C:\files` folder and
attach them to the new email. The new email will be available in the users drafts folder with the subject 
`Attachment Companion Generated Message`. Using the webapp, api or other tools offered by DeliverySlip, the client
can then send the email from those workflows.

Note that if you have an existing email that you would like to upload the attachments to, add the `-g` parameter to
the above command specifying the message guid you would like the attachments uploaded to

### Download Bulk Attachments
```$xslt
java -jar ./attachmentCompanion-1.0.jar -m download -f C:\AttachmentDownloads -u username@domain.com -p secretpassword -s mysuperservice -g a6fccfeae3444d70ac48e168bbc116e1
```
The above command will download all of the attachments belonging to the message with the guid 
`a6fccfeae3444d70ac48e168bbc116e1`. If you are uisng the Attachment Companion to upload and generate a new message
this guid will be printed in the console output while it is uploading. It will then download all the attachments and 
place them into the `C:\AttachmentDownloads` folder. The username, password and servicecode are to authenticate with 
the API and find the appropriate inbox.

### Upload Attachment With Client Side Encryption
```$xslt
java -jar ./attachmentCompanion-1.0.jar -m upload -f C:\files\mysensitivefile.txt -u username@domain.com -p secretpassword -s mysuperservice -k myencryptionpassword
```
The above command will upload the specific file `mysensitivefile.txt` to a new message that will be created and saved into
the drafts folder of the account. Before it is uploaded though it will be encrypted on the client side using the password
`myencryptionpassword`. A SHA256 hash is generated off of this password which is then used as the key to encrypt the files
content. The encrypted file will then be uploaded and attached with the name `mysensitivefile.dat`. 

By default AttachmentCompanion encrypts using AES. By adding the `-e` parameter you can specify either `AES` or `DES`
to explicitly set the encryption algorithm used for your files. AES is the preferred method as the encryption password
will then be the entire SHA256 hash generated off of the passed in password. Due to limitations of DES, only the first
8 bytes of the SHA256 hash is used.

### Download Attachment with Client Side Encryption
```$xslt
java -jar ./attachmentCompanion-1.0.jar -m download -f C:\AttachmentDownloads -u username@domain.com -p secretpassword -s mysuperservice -g a6fccfeae3444d70ac48e168bbc116e1 -k myencryptionpassword
```
If you have uploaded your attachments with encryption via the Attachment Companion, you will need a way to download and
decrypt them aswell. The Attachment Companion does this with the above command. By simply supplying the encryption
password and the message guid, the Attachment Companion will download all of the encrypted `.dat` files into
the `C:\AttachmentDownloads` folder and then decrypt each of them.

Note that if you specified another encryption algorithm such as DES on upload, you will need to specify that parameter
aswell for the download. By default AES is used.

