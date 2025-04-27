import React, { useState } from 'react';
import { ZipWriter, BlobReader, BlobWriter } from '@zip.js/zip.js';
import { Form } from '@heroui/form';
import { Input } from "@heroui/input";
import axios from 'axios';
import { Button } from '@heroui/button';

const ImageUploadForm = (props: { onFinished: (url: string) => void }) => {
  const [files, setFiles] = useState([]);
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFiles = Array.from(event.target.files ?? []);
    if (selectedFiles.length > 5) {
      alert('Вы можете выбрать до 5 изображений');
      return;
    }
    setFiles(selectedFiles);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (files.length === 0) {
      alert('Пожалуйста, выберите хотя бы одно изображение');
      return;
    }
    if (!name || !phone) {
      alert('Пожалуйста, заполните имя и телефон');
      return;
    }

    try {
      // Создание zip архива
      const zipWriter = new ZipWriter(new BlobWriter('application/zip'));
      for (const file of files) {
        await zipWriter.add(file.name, new BlobReader(file));
      }
      const zipBlob = await zipWriter.close();

      // Отправка zip файла на сервер вместе с дополнительными полями
      const formData = new FormData();
      formData.append('file', zipBlob, 'images.zip');
      formData.append('name', name);
      formData.append('phone', phone);

      const response = await axios.post('api/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      if (response.status === 200) {
        props.onFinished(response.data);
        // Очистка формы после успешной отправки
        setFiles([]);
        setName('');
        setPhone('');
      }
    } catch (error) {
      console.error('Ошибка при загрузке файлов:', error);
      alert('Произошла ошибка при загрузке файлов');
    }
  };

  return (
    <Form className='flex items-center' onSubmit={handleSubmit}>
      <div className='w-full'>
        <Input
          label="Имя"
          classNames={{ label: "w-[80px]" }}
          labelPlacement='outside-left'
          type="text"
          minLength={9}
          value={name}
          onChange={(e) => setName(e.target.value)} required />
      </div>
      <div className='w-full'>
        <Input
          classNames={{ label: "w-[80px]" }}
          label="Телефон"
          labelPlacement='outside-left'
          type="tel" value={phone} onChange={(e) => setPhone(e.target.value)} required />
      </div>
      <div className='w-full'>
        <Input
          label="Пришлите пример как вы хотите выглядеть"
          labelPlacement='outside'
          type="file" accept="image/*"
          multiple
          onChange={handleFileChange}
          required />
      </div>
      <Button type="submit">Отправить</Button>
    </Form>
  );
};

export default ImageUploadForm;
