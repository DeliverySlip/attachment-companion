package utils

object UserConfiguration {

    var username:String? = null
    var password:String? = null
    var serviceCode:String? = null

    fun loadUserConfiguration(argParser:ArgParser){

        username = argParser.getValue("-u")
        password = argParser.getValue("-p")
        serviceCode = argParser.getValue("-s")

    }

    fun validateUserConfiguration(){

        if(username == null || password == null || serviceCode == null){
            println("Username, Password and ServiceCode Are Required In Order To Login To The Secure Messaging Platform")
        }
    }
}