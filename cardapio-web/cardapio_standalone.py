#!/usr/bin/env python3
"""
🍔 CARDÁPIO WEB - TRAILER UÇÁ (Standalone)
Arquivo único para executar o cardápio web
Autor: Comanda.ai
"""

import http.server
import socketserver
import os
import json
import webbrowser
import requests
from urllib.parse import urlparse
from http.server import SimpleHTTPRequestHandler
import tempfile
import zipfile
import base64

# Configurações
PORT = 8003
HOST = "192.168.2.218"
API_URL = "http://192.168.2.218:8081/api/v1"

# HTML/CSS/JS embarcados (base64)
HTML_CONTENT = """<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Trailer Uça - Cardápio Premium</title>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Bebas+Neue&family=Montserrat:wght@300;400;600;700;800&display=swap');

        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        :root {
            --primary: #1a5f3f;
            --secondary: #f4a020;
            --accent: #ff6b35;
            --dark: #0d2818;
            --light: #fef9f3;
        }

        body {
            font-family: 'Montserrat', sans-serif;
            line-height: 1.6;
            color: var(--dark);
            background: linear-gradient(135deg, #fef9f3 0%, #f5e6d3 100%);
            overflow-x: hidden;
        }

        .hero {
            position: relative;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, var(--dark) 0%, var(--primary) 100%);
            overflow: hidden;
        }

        .hero-content {
            text-align: center;
            color: white;
            z-index: 10;
            padding: 40px;
        }

        .logo {
            font-family: 'Bebas Neue', cursive;
            font-size: 5em;
            letter-spacing: 3px;
            margin-bottom: 20px;
            text-shadow: 4px 4px 8px rgba(0,0,0,0.3);
            background: linear-gradient(45deg, var(--secondary), var(--accent));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }

        .tagline {
            font-size: 1.5em;
            opacity: 0.9;
            margin-bottom: 30px;
            font-weight: 300;
            letter-spacing: 2px;
        }

        .category {
            padding: 80px 20px;
            position: relative;
        }

        .category:nth-child(even) {
            background: rgba(255,255,255,0.5);
            backdrop-filter: blur(10px);
        }

        .category-header {
            text-align: center;
            margin-bottom: 60px;
        }

        .category-title {
            font-family: 'Bebas Neue', cursive;
            font-size: 4em;
            letter-spacing: 2px;
            color: var(--primary);
            margin-bottom: 10px;
            position: relative;
            display: inline-block;
        }

        .category-title::after {
            content: '';
            position: absolute;
            bottom: -10px;
            left: 50%;
            transform: translateX(-50%);
            width: 100px;
            height: 4px;
            background: linear-gradient(90deg, transparent, var(--secondary), transparent);
            border-radius: 2px;
        }

        .category-subtitle {
            color: #666;
            font-size: 1.2em;
            font-weight: 300;
        }

        .menu-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
            gap: 30px;
            max-width: 1200px;
            margin: 0 auto;
        }

        .menu-item {
            background: white;
            border-radius: 20px;
            padding: 30px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.08);
            transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
            position: relative;
            overflow: hidden;
            cursor: pointer;
        }

        .menu-item:hover {
            transform: translateY(-10px) scale(1.02);
            box-shadow: 0 20px 60px rgba(0,0,0,0.15);
        }

        .menu-item.special {
            background: linear-gradient(135deg, var(--primary), var(--dark));
            color: white;
        }

        .menu-item.special::after {
            content: '⭐';
            position: absolute;
            top: 15px;
            right: 15px;
            font-size: 1.5em;
        }

        .item-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 10px;
        }

        .item-name {
            font-size: 1.4em;
            font-weight: 700;
            color: var(--dark);
            margin-bottom: 5px;
        }

        .menu-item.special .item-name {
            color: white;
        }

        .item-price {
            font-size: 1.6em;
            font-weight: 800;
            background: linear-gradient(135deg, var(--secondary), var(--accent));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            white-space: nowrap;
        }

        .item-description {
            font-size: 0.95em;
            color: #777;
            line-height: 1.5;
            font-style: italic;
        }

        .menu-item.special .item-description {
            color: rgba(255,255,255,0.8);
        }

        .loading {
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 50vh;
            font-size: 2em;
            color: var(--primary);
        }

        .error {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            min-height: 50vh;
            padding: 20px;
            text-align: center;
        }

        .error h2 {
            color: var(--accent);
            margin-bottom: 20px;
        }

        .chopp-section {
            padding: 80px 20px;
            background: linear-gradient(135deg, rgba(244, 160, 32, 0.1) 0%, rgba(26, 95, 63, 0.1) 100%);
        }

        .footer {
            background: var(--dark);
            color: white;
            padding: 60px 20px 30px;
            text-align: center;
        }

        .footer-logo {
            font-family: 'Bebas Neue', cursive;
            font-size: 3em;
            margin-bottom: 20px;
            background: linear-gradient(45deg, var(--secondary), var(--accent));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }

        @media (max-width: 768px) {
            .logo {
                font-size: 3.5em;
            }

            .category-title {
                font-size: 3em;
            }

            .menu-grid {
                grid-template-columns: 1fr;
                gap: 20px;
            }
        }
    </style>
</head>
<body>
    <div id="app">
        <section class="hero">
            <div class="hero-content">
                <h1 class="logo">TRAILER UÇA</h1>
                <p class="tagline">Sabor que conquista, tradição que encanta</p>
            </div>
        </section>

        <div id="menu-content">
            <div class="loading">🍽️ Carregando cardápio...</div>
        </div>
    </div>

    <script>
        const API_URL = '/api';

        function formatPrice(valueInCents) {
            const valueInReais = valueInCents / 100;
            return new Intl.NumberFormat('pt-BR', {
                style: 'currency',
                currency: 'BRL'
            }).format(valueInReais);
        }

        function isChopp(itemName) {
            const normalizedName = itemName.toLowerCase();
            return normalizedName.includes('chopp') ||
                   normalizedName.includes('chop') ||
                   normalizedName.includes('radler') ||
                   normalizedName.includes('goré');
        }

        function createMenuItem(item, isSpecial = false) {
            return `
                <div class="menu-item ${isSpecial ? 'special' : ''}">
                    <div class="item-header">
                        <div class="item-name">${item.name}</div>
                        <div class="item-price">${formatPrice(item.value)}</div>
                    </div>
                    ${item.description ? `<div class="item-description">${item.description}</div>` : ''}
                </div>
            `;
        }

        function createCategory(title, subtitle, items, isSpecial = false, className = 'category') {
            if (items.length === 0) return '';

            return `
                <section class="${className}">
                    <div class="category-header">
                        <h2 class="category-title">${title}</h2>
                        <p class="category-subtitle">${subtitle}</p>
                    </div>
                    <div class="menu-grid">
                        ${items.map(item => createMenuItem(item, isSpecial)).join('')}
                    </div>
                </section>
            `;
        }

        async function loadMenu() {
            try {
                const response = await fetch(`${API_URL}/items`);
                if (!response.ok) throw new Error('Falha ao carregar dados');

                const items = await response.json();

                // Filter items with value > 0
                const validItems = items.filter(item => item.value > 0);

                // Separate chopps from drinks
                const chopps = validItems.filter(item =>
                    item.category === 'DRINK' && isChopp(item.name)
                );

                // Group items by category
                const skewers = validItems.filter(item => item.category === 'SKEWER');
                const snacks = validItems.filter(item => item.category === 'SNACK');
                const drinks = validItems.filter(item =>
                    item.category === 'DRINK' && !isChopp(item.name)
                );
                const promotions = validItems.filter(item => item.category === 'PROMOTIONAL');

                // Build HTML
                let html = '';

                html += createCategory('ESPETINHOS', 'Grelhados na perfeição, temperados com paixão', skewers);
                html += createCategory('PORÇÕES', 'Para compartilhar momentos especiais', snacks);
                html += createCategory('CHOPPS UÇÁ', '🍺 Sempre na temperatura perfeita', chopps, true, 'chopp-section');
                html += createCategory('BEBIDAS', 'Geladas na temperatura ideal', drinks);
                html += createCategory('PROMOÇÕES', 'Aproveite nossas ofertas especiais', promotions, true);

                html += `
                    <footer class="footer">
                        <h3 class="footer-logo">TRAILER UÇÁ</h3>
                        <p>📍 Balneário Bica | 📱 (79) 99801-9211</p>
                        <p style="opacity: 0.5; margin-top: 20px;">© 2025 Trailer Uçá. Todos os direitos reservados.</p>
                    </footer>
                `;

                document.getElementById('menu-content').innerHTML = html;

            } catch (error) {
                document.getElementById('menu-content').innerHTML = `
                    <div class="error">
                        <h2>Ops! Algo deu errado</h2>
                        <p>Não foi possível carregar o cardápio.<br>
                        Por favor, verifique se o servidor está rodando e tente novamente.</p>
                        <p style="margin-top: 20px; color: #666;">Erro: ${error.message}</p>
                    </div>
                `;
            }
        }

        // Load menu when page loads
        document.addEventListener('DOMContentLoaded', loadMenu);
    </script>
</body>
</html>"""

class CardapioHTTPHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        # Create temporary directory for serving files
        self.temp_dir = tempfile.mkdtemp()
        super().__init__(*args, directory=self.temp_dir, **kwargs)

    def do_GET(self):
        # Proxy para requisições da API
        if self.path.startswith('/api/'):
            self.proxy_api_request()
        else:
            # Serve the main HTML file
            if self.path in ['/', '/cardapio', '/cardapio/', '/index.html']:
                self.send_response(200)
                self.send_header('Content-Type', 'text/html; charset=utf-8')
                self.send_header('Access-Control-Allow-Origin', '*')
                self.end_headers()
                self.wfile.write(HTML_CONTENT.encode('utf-8'))
            else:
                self.send_error(404, "File not found")

    def proxy_api_request(self):
        """Proxy para requisições da API Kotlin"""
        try:
            # Remove /api do path e faz a requisição para a API real
            api_path = self.path.replace('/api', '')
            response = requests.get(f"{API_URL}{api_path}")

            # Envia a resposta
            self.send_response(response.status_code)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            self.wfile.write(response.content)
        except Exception as e:
            self.send_error(500, f"Erro ao conectar com a API: {str(e)}")

    def end_headers(self):
        """Adiciona headers CORS"""
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        SimpleHTTPRequestHandler.end_headers(self)

def check_api_status():
    """Verifica se a API Kotlin está rodando"""
    try:
        response = requests.get(f"{API_URL}/items", timeout=5)
        return response.status_code == 200
    except:
        return False

def main():
    """Função principal"""
    print("=" * 60)
    print("🍔 CARDÁPIO WEB - TRAILER UÇÁ (Standalone)")
    print("=" * 60)
    print(f"📦 Versão: Arquivo único standalone")
    print(f"📍 Endereço: {HOST}:{PORT}")
    print(f"📡 API: {API_URL}")

    # Verifica se a API está rodando
    print("\n🔍 Verificando status da API...")
    if check_api_status():
        print("✅ API está rodando!")
    else:
        print("⚠️  API não está respondendo em", API_URL)
        print("    Certifique-se de que a CommanderAPI está rodando")
        response = input("\nDeseja continuar mesmo assim? (s/n): ")
        if response.lower() != 's':
            print("👋 Até logo!")
            return

    print(f"\n🚀 Iniciando servidor...")
    print(f"📍 URL local: http://localhost:{PORT}")
    print(f"📍 URL da rede: http://{HOST}:{PORT}")
    print("\nPressione Ctrl+C para parar o servidor\n")

    try:
        with socketserver.TCPServer((HOST, PORT), CardapioHTTPHandler) as httpd:
            # Abre o navegador automaticamente
            webbrowser.open(f"http://localhost:{PORT}")
            httpd.serve_forever()
    except KeyboardInterrupt:
        print("\n\n👋 Servidor encerrado")
    except Exception as e:
        print(f"❌ Erro ao iniciar servidor: {e}")

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n👋 Até logo!")
    except Exception as e:
        print(f"❌ Erro: {e}")