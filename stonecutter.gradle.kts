plugins {
    id("dev.kikugie.stonecutter")
    id("me.modmuss50.mod-publish-plugin") version "2.2.0" apply false
}

stonecutter active "26.2" /* [SC] DO NOT EDIT */

stonecutter tasks {
    order("publishModrinth")
}