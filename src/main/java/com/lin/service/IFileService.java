package com.lin.service;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
   String upload(MultipartFile file , String path);

   String upload2(MultipartFile file, String path);
}
