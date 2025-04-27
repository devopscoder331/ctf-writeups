import { Link } from "@heroui/link";
import { Snippet } from "@heroui/snippet";
import { Code } from "@heroui/code";
import { button as buttonStyles } from "@heroui/theme";

import { siteConfig } from "@/config/site";
import { title, subtitle } from "@/components/primitives";
import { GithubIcon } from "@/components/icons";
import { Image } from "@heroui/image";
import { Button, ButtonGroup } from "@heroui/button";
import DefaultLayout from "@/layouts/default";
import { Card, CardHeader, CardBody, CardFooter } from "@heroui/card";
import ImageUploadForm from "./_form";
import { Modal, ModalBody, ModalContent, ModalFooter, ModalHeader, useDisclosure } from "@heroui/modal";
import { useState } from "react";


export default function IndexPage() {
  const { isOpen, onOpen, onClose } = useDisclosure();
  const [url, setUrl] = useState("");

  function onFinish(url: string) {
    setUrl(url);
    onOpen();
  }

  return (
    <DefaultLayout>
      <section className="z-20 bg-cover  bg-dark-section-bg flex flex-col items-center justify-center w-full dark bg-background">
        <div className="w-full h-full bg-[#00000068] backdrop-blur-sm flex-col flex items-center gap-[18px] sm:gap-6 p-6">

          <div className=" text-center text-[clamp(40px,10vw,44px)] font-bold leading-[1.2] tracking-tighter sm:text-[64px] ">
            <div className="bg-hero-section-title bg-clip-text text-transparent">
              Лапы-ножницы<br /> Твой путь к совершенству
            </div>
          </div>
          <p className="text-center py-12 font-normal leading-7 text-default-500 sm:w-[466px] sm:text-[18px]">
            Представляем наш уникальный барбершоп, где забота и стиль сочетаются для всех, кто ценит красоту и ухоженность. Мы понимаем, что каждому из нас нужен особенный уход, поэтому предлагаем широкий спектр услуг, разработанных с учетом индивидуальных потребностей.
          </p>
        </div>
      </section>
      <section className="z-20 flex flex-row items-center justify-center w-full gap-[18px] sm:gap-6 p-6  ">


        <Card className="w-[250px] h-[400px]" isPressable shadow="sm" onPress={() => console.log("item pressed")}>
          <CardBody className="overflow-visible p-0">
            <Image
              className="w-full object-cover h-[240px]"
              radius="lg"
              shadow="sm"
              src="/images/cut.png"
              width="100%"
            />
          </CardBody>
          <CardFooter className="text-small flex-col justify-between">
            <div>
              <b>Стрижка</b></div>
            <div>
              <p className="text-default-500">опытные мастера создадут стильный и ухоженный образ, используя только качественные инструменты и безопасные методы стрижки.</p>
            </div>

          </CardFooter>
        </Card>

        <Card className="w-[250px]  h-[400px]" isPressable shadow="sm" onPress={() => console.log("item pressed")}>
          <CardBody className="overflow-visible p-0">
            <Image
              className="w-full object-cover h-[240px]"
              radius="lg"
              shadow="sm"
              src="/images/cool.jpeg"
              width="100%"
            />
          </CardBody>
          <CardFooter className="text-small flex-col justify-between">
            <div>
              <b>Уход за шерсткой</b></div>
            <div>
              <p className="text-default-500">Наши специалисты знают, как правильно ухаживать за волосами, чтобы они оставались здоровыми и блестящими.</p>
            </div>

          </CardFooter>
        </Card>

        <Card className="w-[250px] h-[400px]" isPressable shadow="sm" onPress={() => console.log("item pressed")}>
          <CardBody className="overflow-visible p-0">
            <Image
              className="w-full object-cover h-[240px]"
              radius="lg"
              shadow="sm"
              src="/images/color.jpeg"
              width="100%"
            />
          </CardBody>
          <CardFooter className="text-small flex-col justify-between">
            <div>
              <b>Стань уникальным</b></div>
            <div>
              <p className="text-default-500">
                Для тех, кто хочет выделиться, у нас есть различные специальные процедуры, такие как окрашивание шерсти или создание уникальных укладок.
              </p>
            </div>

          </CardFooter>
        </Card>


      </section>
      <section className="bg-[url(https://avatars.mds.yandex.net/i?id=0b237f3e0e91bbeb5d3c687be4f760d0_l-8242815-images-thumbs&n=13)] z-20 flex flex-col items-center justify-center w-full dark bg-background">

        <div className="w-full bg-[#000000b8] h-full backdrop-blur-sm flex-col flex items-center gap-[18px] sm:gap-6 p-6">
          <div className="text-center text-[clamp(40px,10vw,44px)] font-bold leading-[1.2] tracking-tighter sm:text-[64px]">
            <div className="bg-hero-section-title bg-clip-text text-transparent">
              Записаться
            </div>
          </div>
          <ImageUploadForm onFinished={onFinish} />
        </div>
      </section>


      <Modal backdrop="blur" isOpen={isOpen} onClose={onClose}>
        <ModalContent>
          {(onClose) => (
            <>
              <ModalHeader className="flex flex-col gap-1">Вы успешно записались</ModalHeader>
              <ModalBody>
                В ближайшее время мы с вами свяжемся. И вы будете выглядеть вот так:

                <Image src={url} />

              </ModalBody>
              <ModalFooter>
                <Button color="primary" onPress={onClose}>
                  Отлично
                </Button>
              </ModalFooter>
            </>
          )}
        </ModalContent>
      </Modal>
    </DefaultLayout>
  );
}
