import { useQuery } from '@tanstack/react-query';
import { itemsService } from '../services/api';
import { Item, ItemCategory } from '../types/item';
import { isChopp } from '../utils/formatters';

export const useItems = () => {
  const { data: items = [], isLoading, error } = useQuery({
    queryKey: ['items'],
    queryFn: itemsService.getAllItems,
  });

  // Filter out items with value 0 and separate chopps
  const processedItems = items.filter(item => item.value > 0);

  // Separate chopps from drinks
  const chopps = processedItems.filter(
    item => item.category === ItemCategory.DRINK && isChopp(item.name)
  );

  // Get regular drinks (excluding chopps)
  const regularDrinks = processedItems.filter(
    item => item.category === ItemCategory.DRINK && !isChopp(item.name)
  );

  // Group items by category
  const itemsByCategory = {
    [ItemCategory.SKEWER]: processedItems.filter(item => item.category === ItemCategory.SKEWER),
    [ItemCategory.SNACK]: processedItems.filter(item => item.category === ItemCategory.SNACK),
    [ItemCategory.DRINK]: regularDrinks,
    [ItemCategory.PROMOTIONAL]: processedItems.filter(item => item.category === ItemCategory.PROMOTIONAL),
  };

  return {
    items: processedItems,
    itemsByCategory,
    chopps,
    isLoading,
    error,
  };
};