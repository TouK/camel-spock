package pl.touk.camelSpock.utils

class CamelSpockUtils {

    String fromClassPath(String fileName){
        this.getClass().getResource("/${fileName}").text
    }

}
