//
//  ContentView.swift
//  iosApp
//
//  Created by Leonardo Paixão on 05/03/25.
//

import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    let onLoaded: () -> Void
    
    func makeUIViewController(context: Context) -> UIViewController {
        let viewController = MainViewControllerKt.MainViewController()
        
        // Simula o carregamento e chama onLoaded após um pequeno delay
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            onLoaded()
        }
        
        return viewController
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct LoadingView: View {
    @State private var rotation: Double = 0
    @State private var opacity: Double = 0.5
    @State private var dotCount: Int = 0
    
    var body: some View {
        ZStack {
            Color.white
                .ignoresSafeArea()
            
            VStack(spacing: 40) {
                // Título principal
                Text("ComandaAi!")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .foregroundColor(Color.black)
                    .opacity(opacity)
                    .animation(
                        Animation.easeInOut(duration: 1.5)
                            .repeatForever(autoreverses: true),
                        value: opacity
                    )
                
                // Spinner de loading
                ZStack {
                    Circle()
                        .stroke(Color.gray.opacity(0.2), lineWidth: 4)
                        .frame(width: 60, height: 60)
                    
                    Circle()
                        .trim(from: 0, to: 0.6)
                        .stroke(
                            LinearGradient(
                                gradient: Gradient(colors: [
                                    Color(red: 0.2, green: 0.6, blue: 1.0),
                                    Color(red: 0.8, green: 0.2, blue: 1.0)
                                ]),
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            ),
                            style: StrokeStyle(lineWidth: 4, lineCap: .round)
                        )
                        .frame(width: 60, height: 60)
                        .rotationEffect(.degrees(rotation))
                        .animation(
                            Animation.linear(duration: 1.2)
                                .repeatForever(autoreverses: false),
                            value: rotation
                        )
                }
                
                // Dots animados
                HStack(spacing: 8) {
                    ForEach(0..<3) { index in
                        Circle()
                            .frame(width: 8, height: 8)
                            .foregroundColor(Color.gray)
                            .opacity(dotCount == index ? 1.0 : 0.3)
                            .animation(.easeInOut(duration: 0.6), value: dotCount)
                    }
                }
                
                Text("Carregando...")
                    .font(.callout)
                    .foregroundColor(.gray)
            }
        }
        .onAppear {
            rotation = 360
            opacity = 1.0
            
            // Anima os dots
            Timer.scheduledTimer(withTimeInterval: 0.6, repeats: true) { _ in
                dotCount = (dotCount + 1) % 3
            }
        }
    }
}

struct ContentView: View {
    @State private var isLoading = true
    
    var body: some View {
        ZStack {
            if isLoading {
                LoadingView()
                    .transition(.opacity)
            } else {
                ComposeView(onLoaded: {})
                    .ignoresSafeArea(.keyboard)
                    .ignoresSafeArea(.container)
                    .transition(.opacity)
            }
        }
        .onAppear {
            // Loading mínimo de 1 segundo para suavizar transição
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                withAnimation(.easeInOut(duration: 0.3)) {
                    isLoading = false
                }
            }
        }
    }
}
