import http from "../config/http-common";
import type { Customer } from "../types/Customer";
import type { AxiosResponse } from "axios";

class CustomerService {
  private endpoint: string = "/customers";

  getAll(): Promise<AxiosResponse<Customer[]>> {
    return http.get<Customer[]>(`${this.endpoint}`);
  }

  create(data: Customer): Promise<AxiosResponse<Customer>> {
    return http.post<Customer>(`${this.endpoint}`, data);
  }

  update(data: Customer): Promise<AxiosResponse<Customer>> {
    return http.put<Customer>(`${this.endpoint}`, data);
  }

  delete(id: number): Promise<AxiosResponse<void>> {
    return http.delete<void>(`${this.endpoint}/${id}`);
  }
}

export default new CustomerService();
