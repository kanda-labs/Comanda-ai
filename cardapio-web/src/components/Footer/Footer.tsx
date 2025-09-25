import React from 'react';
import {
  FooterContainer,
  FooterContent,
  FooterLogo,
  FooterInfo,
  SocialLinks,
  SocialLink,
  Copyright
} from './Footer.styles';

export const Footer: React.FC = () => {
  return (
    <FooterContainer>
      <FooterContent>
        <FooterLogo>TRAILER UÇA</FooterLogo>
        <FooterInfo>
          📍 Rua Principal, Centro | 📱 (88) 9999-9999
        </FooterInfo>
        <SocialLinks>
          <SocialLink href="#" aria-label="Instagram">
            📷
          </SocialLink>
          <SocialLink href="#" aria-label="WhatsApp">
            💬
          </SocialLink>
          <SocialLink href="#" aria-label="Facebook">
            👍
          </SocialLink>
        </SocialLinks>
        <Copyright>
          © 2025 Trailer Uçá. Todos os direitos reservados.
        </Copyright>
      </FooterContent>
    </FooterContainer>
  );
};