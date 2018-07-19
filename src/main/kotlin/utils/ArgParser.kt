package utils

class ArgParser(private val arguments:Array<String>) {


    fun getValue(key:String):String?{

        arguments.forEachIndexed { index, s ->
            if(key.equals(s, ignoreCase = true)){
                return arguments[index + 1]
            }
        }

        return null

    }

    fun keyExists(key:String):Boolean{

        arguments.forEach {
            if(key == it){
                return true
            }
        }

        return false
    }
}