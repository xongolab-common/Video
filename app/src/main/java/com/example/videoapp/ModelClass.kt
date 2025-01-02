package com.example.videoapp



data class HomeMediaDetailResponse2(
    val payload: List<Payload> = listOf(),
){
    data class Payload(
        val subtitle: String = "",
        val subtitlePath: String = "",
        val subtitles: ArrayList<Subtitle> = arrayListOf(),
    ){
        data class Subtitle(
            val `file`: String = "",
            val language: String = "",
        )
    }
}


data class EpisodeListResponse(
    val payload: ArrayList<Payload> = arrayListOf()
) {
    data class Payload(
        val defaultImage: String = "",
        val description: String = "",
        val episodeNo: String = "",
        val id: String = "",
        val mainVideoDuration: String = "0",
        val video: String = "",
        val standardVideo: String = "",
        var downloadStatus : String = ""
    )
}