import React from 'react';
import { Item } from '../../types/item';
import { formatPrice } from '../../utils/formatters';
import {
  MenuItemCard,
  ItemHeader,
  ItemName,
  ItemPrice,
  ItemDescription
} from './MenuItem.styles';

interface MenuItemProps {
  item: Item;
  isSpecial?: boolean;
}

export const MenuItem: React.FC<MenuItemProps> = ({ item, isSpecial = false }) => {
  return (
    <MenuItemCard $isSpecial={isSpecial}>
      <ItemHeader>
        <ItemName $isSpecial={isSpecial}>{item.name}</ItemName>
        <ItemPrice>{formatPrice(item.value)}</ItemPrice>
      </ItemHeader>
      {item.description && (
        <ItemDescription $isSpecial={isSpecial}>
          {item.description}
        </ItemDescription>
      )}
    </MenuItemCard>
  );
};