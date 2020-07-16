package com.excubivue.cubivue_scanners.scanner.models

data class ScanResult(var result: String = "", var scannerType: String = "") {

    override fun toString(): String {
        return "{" +
                "  \"result\": \"$result\"," +
                "  \"scannerType\": $scannerType" +
                "}"
    }
}