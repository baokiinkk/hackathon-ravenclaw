package com.baokiin.hackathon.extension

import android.content.Intent

fun Intent.popAndNewTask() {
    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
}

// flow : main ->A ->B -> C -> D -> A
// output: onDestroy c ->onDestroy B ->onDestroy A->onCreate A -> onDestroy D
fun Intent.popAndNewScreen() {
    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
}

// flow : main ->A ->B -> C -> D->A
// output: main ->A ->B -> C -> D -> A  -> luôn luôn tạo mới 1 activity trên top của task
fun Intent.startSingleTop() {
    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

}

// flow : main ->A ->B -> C -> D->
// output: onDestroy C ->onDestroy B ->onNewIntent A -> onDestroy D
fun Intent.popAndKeepScreen() {
    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

}