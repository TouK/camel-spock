package pl.touk.camelSpock.utils

class CamelSpockUtils {

    String fromClassPath(String fileName){
        this.class.getResource("/${fileName}").text
    }

}
