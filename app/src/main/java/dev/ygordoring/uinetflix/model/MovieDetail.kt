package dev.ygordoring.uinetflix.model

data class MovieDetail(
    val movie: Movie,
    val similar: List<Movie>
)
