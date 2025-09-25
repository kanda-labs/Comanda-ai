import axios from 'axios';
import { Item } from '../types/item';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://192.168.2.218:8081/api/v1';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const itemsService = {
  async getAllItems(): Promise<Item[]> {
    const response = await api.get<Item[]>('/items');
    return response.data;
  },

  async getItemById(id: number): Promise<Item> {
    const response = await api.get<Item>(`/items/${id}`);
    return response.data;
  },
};

export default api;