package com.example.demo.web

import com.example.demo.geojson.model.MyPoint

data class RouteBuildModel(var start: MyPoint = MyPoint(), var end: MyPoint = MyPoint())