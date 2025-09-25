import React from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import styled from 'styled-components';
import { GlobalStyles } from './styles/global';
import { Hero } from './components/Hero/Hero';
import { CategorySection } from './components/CategorySection/CategorySection';
import { ChoppSection } from './components/ChoppSection/ChoppSection';
import { Footer } from './components/Footer/Footer';
import { useItems } from './hooks/useItems';
import { ItemCategory } from './types/item';

const queryClient = new QueryClient();

const AppContainer = styled.div`
  min-height: 100vh;
`;

const LoadingContainer = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  font-size: 2em;
  color: #1a5f3f;
`;

const ErrorContainer = styled.div`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
  text-align: center;
`;

const ErrorTitle = styled.h2`
  color: #ff6b35;
  margin-bottom: 20px;
`;

const ErrorMessage = styled.p`
  color: #666;
  max-width: 600px;
  line-height: 1.6;
`;

function MenuContent() {
  const { itemsByCategory, chopps, isLoading, error } = useItems();

  if (isLoading) {
    return (
      <LoadingContainer>
        🍽️ Carregando cardápio...
      </LoadingContainer>
    );
  }

  if (error) {
    return (
      <ErrorContainer>
        <ErrorTitle>Ops! Algo deu errado</ErrorTitle>
        <ErrorMessage>
          Não foi possível carregar o cardápio.
          Por favor, verifique se o servidor está rodando e tente novamente.
        </ErrorMessage>
      </ErrorContainer>
    );
  }

  return (
    <>
      <Hero />

      <CategorySection
        id="espetinhos"
        category={ItemCategory.SKEWER}
        items={itemsByCategory[ItemCategory.SKEWER]}
      />

      <CategorySection
        id="porcoes"
        category={ItemCategory.SNACK}
        items={itemsByCategory[ItemCategory.SNACK]}
      />

      <ChoppSection chopps={chopps} />

      <CategorySection
        id="bebidas"
        category={ItemCategory.DRINK}
        items={itemsByCategory[ItemCategory.DRINK]}
      />

      <CategorySection
        id="promocoes"
        category={ItemCategory.PROMOTIONAL}
        items={itemsByCategory[ItemCategory.PROMOTIONAL]}
        isSpecial={true}
      />

      <Footer />
    </>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <GlobalStyles />
      <AppContainer>
        <MenuContent />
      </AppContainer>
    </QueryClientProvider>
  );
}

export default App
