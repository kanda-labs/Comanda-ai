import React from 'react';
import { Item, ItemCategory, categoryTitles } from '../../types/item';
import { MenuItem } from '../MenuItem/MenuItem';
import {
  CategoryContainer,
  CategoryHeader,
  CategoryTitle,
  CategorySubtitle,
  MenuGrid
} from './CategorySection.styles';

interface CategorySectionProps {
  category: ItemCategory;
  items: Item[];
  id?: string;
  isSpecial?: boolean;
}

export const CategorySection: React.FC<CategorySectionProps> = ({
  category,
  items,
  id,
  isSpecial = false
}) => {
  const { title, subtitle } = categoryTitles[category];

  if (items.length === 0) return null;

  return (
    <CategoryContainer id={id}>
      <CategoryHeader>
        <CategoryTitle>{title}</CategoryTitle>
        <CategorySubtitle>{subtitle}</CategorySubtitle>
      </CategoryHeader>
      <MenuGrid>
        {items.map((item) => (
          <MenuItem
            key={item.id}
            item={item}
            isSpecial={isSpecial || category === ItemCategory.PROMOTIONAL}
          />
        ))}
      </MenuGrid>
    </CategoryContainer>
  );
};