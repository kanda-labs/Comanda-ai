#!/usr/bin/env python3
"""
🍔 CARDÁPIO WEB - TRAILER UÇÁ (Ultra Simple)
Arquivo único sem dependências externas
Autor: Comanda.ai
"""

import http.server
import socketserver
import webbrowser
import urllib.request
import json
import os
import mimetypes
from http.server import SimpleHTTPRequestHandler

# Configurações
PORT = 8003
HOST = "0.0.0.0"  # Aceita conexões de qualquer IP
API_URL = "http://192.168.2.218:8081/api/v1"

# HTML/CSS/JS completo
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

        /* Hero Section */
        .hero {
            position: relative;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, var(--dark) 0%, var(--primary) 100%);
            background-image: url('/header.png');
            background-size: cover;
            background-position: center;
            background-blend-mode: overlay;
            overflow: hidden;
        }

        .hero::before {
            content: '🍖';
            position: absolute;
            font-size: 300px;
            opacity: 0.03;
            top: -50px;
            right: -100px;
            animation: float 6s ease-in-out infinite;
        }

        .hero::after {
            content: '🍺';
            position: absolute;
            font-size: 250px;
            opacity: 0.03;
            bottom: -50px;
            left: -80px;
            animation: float 8s ease-in-out infinite reverse;
        }

        @keyframes float {
            0%, 100% { transform: translateY(0) rotate(0deg); }
            50% { transform: translateY(-30px) rotate(10deg); }
        }

        .hero-content {
            text-align: center;
            color: white;
            z-index: 10;
            padding: 40px;
            animation: heroEntrance 1s ease-out;
        }

        @keyframes heroEntrance {
            from {
                opacity: 0;
                transform: translateY(50px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
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
            animation: glow 3s ease-in-out infinite;
        }

        @keyframes glow {
            0%, 100% { filter: brightness(1); }
            50% { filter: brightness(1.2); }
        }

        .tagline {
            font-size: 1.5em;
            opacity: 0.9;
            margin-bottom: 30px;
            font-weight: 300;
            letter-spacing: 2px;
        }

        .cta-button {
            display: inline-block;
            padding: 15px 40px;
            background: linear-gradient(135deg, var(--secondary) 0%, var(--accent) 100%);
            color: white;
            border: none;
            border-radius: 50px;
            font-weight: 600;
            font-size: 1.1em;
            cursor: pointer;
            transition: all 0.3s ease;
            box-shadow: 0 10px 30px rgba(244, 160, 32, 0.3);
            animation: pulse 2s infinite;
            font-family: 'Montserrat', sans-serif;
        }

        @keyframes pulse {
            0%, 100% { transform: scale(1); }
            50% { transform: scale(1.05); }
        }

        .cta-button:hover {
            transform: translateY(-3px) scale(1.05);
            box-shadow: 0 15px 40px rgba(244, 160, 32, 0.4);
        }

        /* Categories */
        .category {
            padding: 80px 20px;
            position: relative;
        }

        .category:nth-child(even) {
            background: rgba(255,255,255,0.5);
            backdrop-filter: blur(10px);
        }

        .chopp-section {
            background: linear-gradient(135deg, rgba(244, 160, 32, 0.1) 0%, rgba(26, 95, 63, 0.1) 100%);
        }

        .porcoes-section {
            background-image: url('/image-acompanhamentos.png');
            background-size: cover;
            background-position: center;
            background-attachment: fixed;
            position: relative;
        }

        .porcoes-section::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(254, 249, 243, 0.95);
            backdrop-filter: blur(2px);
            z-index: 1;
        }

        .porcoes-section > * {
            position: relative;
            z-index: 2;
        }

        .category-header {
            text-align: center;
            margin-bottom: 60px;
            animation: fadeInUp 0.8s ease-out;
        }

        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
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
            margin-top: 20px;
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
            animation: fadeIn 0.6s ease-out backwards;
        }

        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }

        .menu-item::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 5px;
            height: 100%;
            background: linear-gradient(180deg, var(--primary), var(--secondary));
            transition: width 0.3s ease;
        }

        .menu-item:hover::before {
            width: 100%;
            opacity: 0.1;
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
            animation: sparkle 2s ease-in-out infinite;
        }

        @keyframes sparkle {
            0%, 100% { transform: scale(1) rotate(0deg); }
            50% { transform: scale(1.2) rotate(180deg); }
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
            transition: color 0.3s ease;
        }

        .menu-item:hover .item-name {
            color: var(--primary);
        }

        .menu-item.special .item-name {
            color: white;
        }

        .menu-item.special:hover .item-name {
            color: var(--secondary);
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

        /* Loading and Error */
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
            font-family: 'Bebas Neue', cursive;
            font-size: 3em;
        }

        /* Footer */
        .footer {
            background: var(--dark);
            color: white;
            padding: 60px 20px 30px;
            text-align: center;
            position: relative;
            overflow: hidden;
        }

        .footer::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: linear-gradient(135deg, transparent 30%, rgba(244, 160, 32, 0.1) 100%);
            pointer-events: none;
        }

        .footer-content {
            position: relative;
            z-index: 1;
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

        .footer-info {
            font-size: 1.1em;
            opacity: 0.8;
            margin-bottom: 30px;
        }

        .copyright {
            opacity: 0.5;
            font-size: 0.9em;
            padding-top: 20px;
            border-top: 1px solid rgba(255,255,255,0.1);
        }

        /* Responsive */
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

            .tagline {
                font-size: 1.2em;
            }
        }

        /* Animação de entrada dos items */
        .menu-item:nth-child(1) { animation-delay: 0.1s; }
        .menu-item:nth-child(2) { animation-delay: 0.2s; }
        .menu-item:nth-child(3) { animation-delay: 0.3s; }
        .menu-item:nth-child(4) { animation-delay: 0.4s; }
        .menu-item:nth-child(5) { animation-delay: 0.5s; }
        .menu-item:nth-child(6) { animation-delay: 0.6s; }
    </style>
</head>
<body>
    <div id="app">
        <section class="hero">
            <div class="hero-content">
                <h1 class="logo">TRAILER UÇÁ</h1>
                <p class="tagline">Brinde melhor, brinde com Uça!</p>
                <button class="cta-button" onclick="scrollToMenu()">Confira o Cardápio</button>
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
                   normalizedName.includes('goré') ||
                   normalizedName.includes('vinho');
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
                if (!response.ok) throw new Error(`HTTP ${response.status}: ${response.statusText}`);

                const items = await response.json();
                console.log('Items carregados:', items.length);

                // Filter items with value > 0
                const validItems = items.filter(item => item.value > 0);
                console.log('Items válidos:', validItems.length);

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

                console.log('Categorias:', { skewers: skewers.length, snacks: snacks.length, chopps: chopps.length, drinks: drinks.length, promotions: promotions.length });

                // Build HTML
                let html = '';

                html += createCategory('ESPETINHOS', 'Grelhados na perfeição, temperados com paixão', skewers);
                html += createCategory('PORÇÕES', 'Para compartilhar momentos especiais', snacks, false, 'category porcoes-section');

                if (chopps.length > 0) {
                    html += createCategory('CHOPPS UÇÁ', '🍺 Sempre na temperatura perfeita', chopps, true, 'category chopp-section');
                }

                html += createCategory('BEBIDAS', 'Geladas na temperatura ideal', drinks);
                html += createCategory('PROMOÇÕES IMPERDÍVEIS', 'Aproveite nossas ofertas especiais', promotions, true);

                html += `
                    <footer class="footer">
                        <div class="footer-content">
                            <h3 class="footer-logo">TRAILER UÇÁ</h3>
                            <p class="footer-info">
                                📍 Balneário Bica | 📱 (79) 99801-9211
                            </p>
                            <div class="copyright">
                                © 2025 Trailer UçÁ. Todos os direitos reservados.
                            </div>
                        </div>
                    </footer>
                `;

                document.getElementById('menu-content').innerHTML = html;

            } catch (error) {
                console.error('Erro ao carregar menu:', error);
                document.getElementById('menu-content').innerHTML = `
                    <div class="error">
                        <h2>Ops! Algo deu errado</h2>
                        <p>Não foi possível carregar o cardápio.<br>
                        Por favor, verifique se o servidor está rodando e tente novamente.</p>
                        <p style="margin-top: 20px; color: #666; font-size: 0.9em;">
                            Erro técnico: ${error.message}
                        </p>
                        <button onclick="loadMenu()" style="
                            margin-top: 30px;
                            padding: 15px 30px;
                            background: linear-gradient(135deg, var(--secondary), var(--accent));
                            color: white;
                            border: none;
                            border-radius: 25px;
                            font-size: 1.1em;
                            cursor: pointer;
                            transition: transform 0.3s ease;
                        " onmouseover="this.style.transform='scale(1.05)'" onmouseout="this.style.transform='scale(1)'">
                            🔄 Tentar novamente
                        </button>
                    </div>
                `;
            }
        }

        // Função para scroll suave até o cardápio
        function scrollToMenu() {
            // Aguarda o menu ser carregado se necessário
            const menuContent = document.getElementById('menu-content');
            if (menuContent && menuContent.children.length > 0) {
                // Se o menu já foi carregado, faz scroll para a primeira seção
                const firstSection = menuContent.querySelector('.category');
                if (firstSection) {
                    firstSection.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            } else {
                // Se o menu ainda não foi carregado, faz scroll para o conteúdo
                menuContent.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        }

        // Load menu when page loads
        document.addEventListener('DOMContentLoaded', function() {
            console.log('Página carregada, iniciando carregamento do menu...');
            loadMenu();
        });
    </script>
</body>
</html>"""

class CardapioHTTPHandler(SimpleHTTPRequestHandler):
    def do_GET(self):
        # Proxy para requisições da API
        if self.path.startswith('/api/'):
            self.proxy_api_request()
        # Serve imagens
        elif self.path in ['/header.png', '/image-acompanhamentos.png', '/footer.png']:
            self.serve_image(self.path[1:])  # Remove a barra inicial
        else:
            # Serve the main HTML file
            if self.path in ['/', '/cardapio', '/cardapio/', '/index.html']:
                self.send_response(200)
                self.send_header('Content-Type', 'text/html; charset=utf-8')
                self.send_header('Access-Control-Allow-Origin', '*')
                self.end_headers()
                self.wfile.write(HTML_CONTENT.encode('utf-8'))
            else:
                self.send_error(404, "Arquivo não encontrado")

    def serve_image(self, image_name):
        """Serve imagens estáticas"""
        try:
            if os.path.exists(image_name):
                # Determina o tipo MIME
                mime_type, _ = mimetypes.guess_type(image_name)
                if mime_type is None:
                    mime_type = 'application/octet-stream'

                # Lê o arquivo
                with open(image_name, 'rb') as f:
                    image_data = f.read()

                # Envia resposta
                self.send_response(200)
                self.send_header('Content-Type', mime_type)
                self.send_header('Content-Length', len(image_data))
                self.send_header('Access-Control-Allow-Origin', '*')
                self.send_header('Cache-Control', 'public, max-age=3600')  # Cache por 1 hora
                self.end_headers()
                self.wfile.write(image_data)
            else:
                self.send_error(404, f"Imagem {image_name} não encontrada")
        except Exception as e:
            self.send_error(500, f"Erro ao servir imagem: {str(e)}")

    def proxy_api_request(self):
        """Proxy para requisições da API Kotlin usando urllib"""
        try:
            # Remove /api do path e faz a requisição para a API real
            api_path = self.path.replace('/api', '')
            url = f"{API_URL}{api_path}"

            # Faz a requisição usando urllib
            req = urllib.request.Request(url)
            with urllib.request.urlopen(req) as response:
                data = response.read()

            # Envia a resposta
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            self.wfile.write(data)

        except urllib.error.HTTPError as e:
            self.send_error(e.code, f"Erro da API: {e.reason}")
        except Exception as e:
            self.send_error(500, f"Erro ao conectar com a API: {str(e)}")

    def end_headers(self):
        """Adiciona headers CORS"""
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')
        SimpleHTTPRequestHandler.end_headers(self)

def check_api_status():
    """Verifica se a API Kotlin está rodando usando urllib"""
    try:
        req = urllib.request.Request(f"{API_URL}/items")
        with urllib.request.urlopen(req, timeout=5) as response:
            return response.status == 200
    except:
        return False

def main():
    """Função principal"""
    print("=" * 60)
    print("🍔 CARDÁPIO WEB - TRAILER UÇÁ (Ultra Simple)")
    print("=" * 60)
    print(f"📦 Versão: Arquivo único sem dependências")
    print(f"📍 Servidor: {HOST}:{PORT}")
    print(f"📡 API: {API_URL}")

    # Verifica se a API está rodando
    print("\n🔍 Verificando status da API...")
    if check_api_status():
        print("✅ API está rodando!")
    else:
        print("⚠️  API não está respondendo em", API_URL)
        print("    Certifique-se de que a CommanderAPI está rodando")
        response = input("\nDeseja continuar mesmo assim? (s/n): ").strip().lower()
        if response != 's':
            print("👋 Até logo!")
            return

    print(f"\n🚀 Iniciando servidor...")
    print(f"📍 URL local: http://localhost:{PORT}")
    print(f"📍 URL da rede: http://{HOST}:{PORT}")
    print(f"📍 Cardápio: http://{HOST}:{PORT}/cardapio")
    print("\n💡 O navegador será aberto automaticamente")
    print("⌨️  Pressione Ctrl+C para parar o servidor\n")

    try:
        with socketserver.TCPServer((HOST, PORT), CardapioHTTPHandler) as httpd:
            print("🟢 Servidor iniciado com sucesso!")

            # Abre o navegador automaticamente
            try:
                webbrowser.open(f"http://localhost:{PORT}/cardapio")
                print("🌐 Navegador aberto!")
            except:
                print("⚠️  Não foi possível abrir o navegador automaticamente")

            print("\n⏰ Aguardando requisições...")
            httpd.serve_forever()

    except OSError as e:
        if e.errno == 48:  # Address already in use
            print(f"❌ Erro: Porta {PORT} já está em uso!")
            print("💡 Dica: Mate o processo que está usando a porta ou escolha outra porta")
        else:
            print(f"❌ Erro de rede: {e}")
    except KeyboardInterrupt:
        print("\n\n👋 Servidor encerrado pelo usuário")
    except Exception as e:
        print(f"❌ Erro inesperado: {e}")

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n👋 Até logo!")
    except Exception as e:
        print(f"❌ Erro fatal: {e}")