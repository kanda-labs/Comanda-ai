//
//  ContentView.swift
//  iosApp
//
//  Created by Leonardo Paixão on 05/03/25.
//

import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea(.keyboard)
                .ignoresSafeArea(.container)
    }
}
