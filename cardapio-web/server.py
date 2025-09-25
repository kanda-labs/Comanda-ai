#!/usr/bin/env python3
"""
Servidor local para o Cardápio Web
Autor: Comanda.ai
Descrição: Servidor HTTP simples para servir o cardápio web com proxy reverso para a API
"""

import http.server
import socketserver
import subprocess
import sys
import os
import signal
import time
import webbrowser
from urllib.parse import urlparse
import requests
from http.server import SimpleHTTPRequestHandler
import json

PORT = 8003
HOST = "192.168.2.199"
API_URL = "http://192.168.2.199:8081/api/v1"

class CardapioHTTPHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory="dist", **kwargs)

    def do_GET(self):
        # Proxy para requisições da API
        if self.path.startswith('/api/'):
            self.proxy_api_request()
        else:
            # Serve arquivos estáticos
            if self.path == '/':
                self.path = '/cardapio/index.html'
            elif self.path == '/cardapio' or self.path == '/cardapio/':
                self.path = '/cardapio/index.html'

            # Remove /cardapio do path para servir do dist
            if self.path.startswith('/cardapio/'):
                self.path = self.path[9:]  # Remove '/cardapio'

            return SimpleHTTPRequestHandler.do_GET(self)

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

def build_project():
    """Compila o projeto React para produção"""
    print("🔨 Compilando projeto React...")
    try:
        subprocess.run(["npm", "run", "build"], check=True)
        print("✅ Build concluído com sucesso!")
        return True
    except subprocess.CalledProcessError:
        print("❌ Erro ao compilar o projeto")
        return False

def start_dev_server():
    """Inicia o servidor de desenvolvimento"""
    print(f"\n🚀 Iniciando servidor de desenvolvimento...")
    print(f"📍 URL local: http://localhost:8003/cardapio")
    print(f"📍 URL da rede: http://{HOST}:8003/cardapio")
    print(f"📡 API URL: {API_URL}")
    print("\nPressione Ctrl+C para parar o servidor\n")

    try:
        # Inicia o servidor Vite de desenvolvimento
        subprocess.run(["npm", "run", "dev"], check=True)
    except KeyboardInterrupt:
        print("\n\n👋 Servidor encerrado")
        sys.exit(0)
    except subprocess.CalledProcessError as e:
        print(f"❌ Erro ao iniciar servidor: {e}")
        sys.exit(1)

def start_production_server():
    """Inicia o servidor de produção"""
    if not os.path.exists("dist"):
        print("📦 Pasta dist não encontrada. Compilando projeto...")
        if not build_project():
            sys.exit(1)

    print(f"\n🚀 Iniciando servidor de produção...")
    print(f"📍 URL local: http://localhost:{PORT}/cardapio")
    print(f"📍 URL da rede: http://{HOST}:{PORT}/cardapio")
    print(f"📡 API URL: {API_URL}")
    print("\nPressione Ctrl+C para parar o servidor\n")

    try:
        with socketserver.TCPServer((HOST, PORT), CardapioHTTPHandler) as httpd:
            # Abre o navegador automaticamente
            webbrowser.open(f"http://localhost:{PORT}/cardapio")
            httpd.serve_forever()
    except KeyboardInterrupt:
        print("\n\n👋 Servidor encerrado")
        sys.exit(0)
    except Exception as e:
        print(f"❌ Erro ao iniciar servidor: {e}")
        sys.exit(1)

def main():
    """Função principal"""
    print("=" * 60)
    print("🍔 CARDÁPIO WEB - TRAILER UÇÁ")
    print("=" * 60)

    # Verifica se a API está rodando
    print("\n🔍 Verificando status da API...")
    if check_api_status():
        print("✅ API está rodando!")
    else:
        print("⚠️  API não está respondendo em", API_URL)
        print("    Certifique-se de que a CommanderAPI está rodando")
        response = input("\nDeseja continuar mesmo assim? (s/n): ")
        if response.lower() != 's':
            sys.exit(0)

    # Menu de opções
    print("\nEscolha o modo de execução:")
    print("1. Servidor de Desenvolvimento (com hot reload)")
    print("2. Servidor de Produção (build otimizado)")
    print("3. Compilar projeto apenas")
    print("4. Sair")

    choice = input("\nOpção: ")

    if choice == "1":
        start_dev_server()
    elif choice == "2":
        start_production_server()
    elif choice == "3":
        build_project()
    elif choice == "4":
        print("👋 Até logo!")
        sys.exit(0)
    else:
        print("❌ Opção inválida")
        sys.exit(1)

if __name__ == "__main__":
    # Muda para o diretório do script
    os.chdir(os.path.dirname(os.path.abspath(__file__)))

    # Instala dependências se necessário
    if not os.path.exists("node_modules"):
        print("📦 Instalando dependências...")
        try:
            subprocess.run(["npm", "install"], check=True)
        except subprocess.CalledProcessError:
            print("❌ Erro ao instalar dependências")
            sys.exit(1)

    main()